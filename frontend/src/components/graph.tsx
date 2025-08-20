import React, { useEffect, useState, useMemo, useRef, useCallback } from "react";
import {
  SigmaContainer,
  useSigma,
  useLoadGraph,
  ControlsContainer,
  ZoomControl,
  FullScreenControl,
  useRegisterEvents,
} from "@react-sigma/core";
import { MultiDirectedGraph } from "graphology";
import { NodeSquareProgram } from "@sigma/node-square";
import "@react-sigma/core/lib/react-sigma.min.css";
import { hashToNumber } from "../utils/hash";
import { GraphData } from "../api-client";

const sigmaStyle = { height: "100vh", width: "100vw" };

interface AnimationState {
  sourceId: string;
  targetId: string;
  startTime: number;
  duration: number;
}

interface LoadGraphProps {
  initialGraphData: GraphData;
}

const LoadGraph = ({ initialGraphData }: LoadGraphProps) => {
  const sigma = useSigma();
  const loadGraph = useLoadGraph();
  const registerEvents = useRegisterEvents();

  const draggedNodeRef = useRef<string | null>(null);
  const isDraggingRef = useRef<boolean>(false);

  const [graphData, setGraphData] = useState<{ nodes: any[]; edges: any[] }>({ nodes: [], edges: [] });
  const [animatingItems, setAnimatingItems] = useState<Record<string, AnimationState>>({});
  const animationFrameId = useRef<number | null>(null);

  const locationDataMap = useMemo(() => {
    const map = new Map();
    initialGraphData.locations.forEach(loc => map.set(loc.id, loc));
    return map;
  }, [initialGraphData.locations]);

  const connectionMap = useMemo(() => {
    const map = new Map();
    initialGraphData.connections.forEach(conn => map.set(conn.sourceId, conn.targetId));
    return map;
  }, [initialGraphData.connections]);

  useEffect(() => {
    const nodes = [
      ...initialGraphData.locations.map(location => ({
        id: location.id,
        x: location.longitude ?? hashToNumber(location.id),
        y: location.latitude ?? hashToNumber(location.id + "random"),
        type: "location",
        label: location.name
      })),
      ...initialGraphData.locations.flatMap(location =>
        (location.items || []).map(item => ({
          id: item.id,
          x: location.longitude ?? hashToNumber(location.id),
          y: location.latitude ?? hashToNumber(location.id + "random"),
          type: "item",
          label: item.name,
          initialLocationId: location.id,
        }))
      )
    ];
    const edges = initialGraphData.connections.map(c => ({ source: c.sourceId, target: c.targetId }));
    setGraphData({ nodes, edges });
  }, [initialGraphData]);

  useEffect(() => {
    const graph = new MultiDirectedGraph();
    graphData.nodes.filter(n => n.type === "location").forEach(({ id, x, y, label }) => {
      graph.addNode(id, { x, y, label, size: 10, color: "#69b3a2" });
    });
    graphData.edges.forEach(e => {
        if (!graph.hasNode(e.source) || !graph.hasNode(e.target)) return;
        graph.addEdge(e.source, e.target, { type: 'arrow', size: 2 });
    });
    graphData.nodes.filter(n => n.type === "item").forEach(n => {
        const { initialLocationId, ...nodeProps } = n;
        graph.addNode(n.id, { ...nodeProps, size: 8, color: "#FF0000", type: "square" });
    });
    loadGraph(graph);

    const initialAnimations = {};
    graphData.nodes.filter(n => n.type === "item").forEach(item => {
      const sourceLoc = locationDataMap.get(item.initialLocationId);
      if (sourceLoc && sourceLoc.speed > 0) {
        const targetId = connectionMap.get(item.initialLocationId);
        if (targetId) {
          const duration = (sourceLoc.length / sourceLoc.speed) * 1000;
          initialAnimations[item.id] = { sourceId: item.initialLocationId, targetId, startTime: Date.now(), duration };
        }
      }
    });
    setAnimatingItems(initialAnimations);
  }, [loadGraph, graphData, locationDataMap, connectionMap]);

  useEffect(() => {
    const animate = () => {
        const graph = sigma.getGraph();
        const currentTime = Date.now();
        let needsRefresh = false;
        const nextAnimatingItems = { ...animatingItems };
        for (const itemId in animatingItems) {
            if (itemId === draggedNodeRef.current) continue;
            const anim = animatingItems[itemId];
            const sourceNode = graph.getNodeAttributes(anim.sourceId);
            const targetNode = graph.getNodeAttributes(anim.targetId);
            if (!sourceNode || !targetNode) continue;
            const progress = Math.min((currentTime - anim.startTime) / anim.duration, 1);
            graph.setNodeAttribute(itemId, "x", sourceNode.x + (targetNode.x - sourceNode.x) * progress);
            graph.setNodeAttribute(itemId, "y", sourceNode.y + (targetNode.y - sourceNode.y) * progress);
            needsRefresh = true;
            if (progress >= 1) {
                const newSourceId = anim.targetId;
                const newSourceLocation = locationDataMap.get(newSourceId);
                const newTargetId = connectionMap.get(newSourceId);
                if (newTargetId && newSourceLocation && newSourceLocation.speed > 0) {
                    const duration = (newSourceLocation.length / newSourceLocation.speed) * 1000;
                    nextAnimatingItems[itemId] = { sourceId: newSourceId, targetId: newTargetId, startTime: Date.now(), duration };
                } else {
                    delete nextAnimatingItems[itemId];
                }
            }
        }
        setAnimatingItems(nextAnimatingItems);
        if (needsRefresh) sigma.refresh();
        animationFrameId.current = requestAnimationFrame(animate);
    };
    animationFrameId.current = requestAnimationFrame(animate);
    return () => { if (animationFrameId.current) cancelAnimationFrame(animationFrameId.current); };
  }, [sigma, animatingItems, locationDataMap, connectionMap]);

  const addNode = useCallback((x: number, y: number) => {
    const newNodeId = crypto.randomUUID();
    const newNode = { id: newNodeId, x, y, type: "location", label: "New Location" };
    const closestNodes = graphData.nodes
      .filter(n => n.type === 'location')
      .map(n => ({ nodeId: n.id, distance: Math.pow(x - n.x, 2) + Math.pow(y - n.y, 2) }))
      .sort((a, b) => a.distance - b.distance)
      .slice(0, 2);
    setGraphData(prev => ({
      nodes: [...prev.nodes, newNode],
      edges: [...prev.edges, ...closestNodes.map(e => ({ source: newNodeId, target: e.nodeId }))],
    }));
  }, [graphData]);

  useEffect(() => {
    const dragEnd = () => {
      if (draggedNodeRef.current) {
        sigma.getSettings().mouseEnabled = true;
        const draggedNode = draggedNodeRef.current;
        const pos = {
          x: sigma.getGraph().getNodeAttribute(draggedNode, "x"),
          y: sigma.getGraph().getNodeAttribute(draggedNode, "y")
        };
        setGraphData(currentGraphData => {
            const newNodes = currentGraphData.nodes.map(node =>
                node.id === draggedNode ? { ...node, x: pos.x, y: pos.y } : node
            );
            return { ...currentGraphData, nodes: newNodes };
        });
      }
      isDraggingRef.current = false;
      draggedNodeRef.current = null;
    };

    registerEvents({
      // --- START OF THE FIX ---
      clickStage: (event) => {
        // Do not add a node if a drag was in progress
        if (!isDraggingRef.current) {
          // CONVERT the viewport coordinates to graph coordinates
          const pos = sigma.viewportToGraph(event.event);
          addNode(pos.x, pos.y);
        }
      },
      // --- END OF THE FIX ---
      downNode: (event) => {
        if (sigma.getGraph().getNodeAttribute(event.node, "type") !== "square") {
          draggedNodeRef.current = event.node;
        }
      },
      mousemove: (event) => {
        if (draggedNodeRef.current) {
          isDraggingRef.current = true;
          sigma.getSettings().mouseEnabled = false;
          event.preventSigmaDefault();
          const pos = sigma.viewportToGraph(event);
          sigma.getGraph().setNodeAttribute(draggedNodeRef.current, "x", pos.x);
          sigma.getGraph().setNodeAttribute(draggedNodeRef.current, "y", pos.y);
        }
      },
      mouseup: () => {
        if (isDraggingRef.current) {
          dragEnd();
        }
      },
      mouseleave: () => {
        if (isDraggingRef.current) {
          dragEnd();
        }
      }
    });
  }, [registerEvents, sigma, addNode]);

  return null;
};

export const DisplayGraph = ({ initialGraphData }: { initialGraphData: GraphData }) => {
  return (
    <SigmaContainer 
        style={sigmaStyle}
        settings={{
            nodeProgramClasses: {
                square: NodeSquareProgram,
            },
        }}
    >
      <LoadGraph initialGraphData={initialGraphData} />
      <ControlsContainer position={"bottom-right"}>
        <ZoomControl />
        <FullScreenControl />
      </ControlsContainer>
    </SigmaContainer>
  );
};