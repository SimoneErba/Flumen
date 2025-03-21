import React, { useEffect, useState } from "react";
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
import { GraphData } from "../hooks/useGraph";
import { hashToNumber } from "../utils/hash";

const sigmaStyle = { height: "100vh", width: "100vw" };

interface LoadGraphProps {
  initialGraphData: GraphData;
}

const LoadGraph = ({initialGraphData}: LoadGraphProps) => {
  const sigma = useSigma();
  const useloadGraph = useLoadGraph();
  const [draggedNode, setDraggedNode] = useState<string | null>(null);
  const [graphData, setGraphData] = useState<{ nodes: any[]; edges: any[] }>({
    nodes: [],
    edges: [],
  });

  useEffect(() => {
    setGraphData({
      nodes: initialGraphData.nodes.map(node => ({
        id: node.id,
        x: node.properties?.x ?? hashToNumber(node.id),
        y: node.properties?.y ?? hashToNumber(node.id + "random"),
        type: node.type,
      })),
      edges: initialGraphData.edges.map(edge => ({
        source: edge.source,
        target: edge.target,
      })),
    });
  }, [initialGraphData]); 
    
  useEffect(() => {
    const graph = new MultiDirectedGraph();
    graphData.nodes.filter(x => x.type === "location").forEach(({ id, x, y }) => {
        graph.addNode(id, {
          label: `Node ${id}`,
          x,
          y,
          size: 10,
          fixed: true,
        });
    });

    graphData.edges.filter(x => x.type === "connected_to").forEach((edge) => {
      graph.addEdge(edge.source, edge.target);
    });
    const locationCoords = {};

    graphData.nodes
    .filter((node) => node.type === "item")
    .forEach(({ id }) => {
      // Find the location this item is connected to
      const edge = graphData.edges.find(
        (e) => e.type === "connected_to" && e.source === id
      );
  
      if (!edge || !locationCoords[edge.target]) {
        console.warn(`No location found for item ${id}`);
        return; // Skip if no connected location
      }
  
      const { x, y } = locationCoords[edge.target];
  
      graph.addNode(id, {
        label: `Node ${id}`,
        x,
        y,
        size: 8,
        color: "#FF0000",
        type: "square",
      });
    });

    console.log("GRAPH", initialGraphData);
    useloadGraph(graph);
    // Animation function to move the item along the edges
    // let t = 0;
    // const speed = 0.01; // Adjust speed as needed
    // const path = ["A", "B", "C", "D", "A"]; // Define the path

    // const animate = () => {
    //   const segment = Math.floor(t) % (path.length - 1);
    //   const startNode = path[segment];
    //   const endNode = path[segment + 1];

    //   const startX = graph.getNodeAttribute(startNode, "x");
    //   const startY = graph.getNodeAttribute(startNode, "y");
    //   const endX = graph.getNodeAttribute(endNode, "x");
    //   const endY = graph.getNodeAttribute(endNode, "y");

    //   const progress = t - Math.floor(t);

    //   const newX = startX + (endX - startX) * progress;
    //   const newY = startY + (endY - startY) * progress;

    //   graph.setNodeAttribute("Item", "x", newX);
    //   graph.setNodeAttribute("Item", "y", newY);

    //   sigma.refresh();

    //   t += speed;
    //   requestAnimationFrame(animate);
    // };

    //animate();
  }, [useloadGraph, graphData]);

  const saveGraph = () => {
    localStorage.setItem("graphPositions", JSON.stringify(graphData));
    alert("Graph layout saved!");
  };

  const addNode = (x: number, y: number) => {
    const newNodeId = crypto.randomUUID();
    const newNode = {
      id: newNodeId,
      x,
      y,
      size: 10,
      fixed: true,
    };

    // Find the two closest nodes to create edges
    const closestNodes = graphData.nodes
      .map((node) => {
        const distance = Math.pow(x - node.x, 2) + Math.pow(y - node.y, 2);
        return { nodeId: node.id, distance };
      })
      .sort((a, b) => a.distance - b.distance)
      .slice(0, 2);

    // Update the graph data
    setGraphData((prevData) => ({
      nodes: [...prevData.nodes, newNode],
      edges: [
        ...prevData.edges,
        ...closestNodes.map((e) => ({
          source: newNodeId,
          target: e.nodeId,
        })),
      ],
    }));
  };

  const registerEvents = useRegisterEvents();

  useEffect(() => {
    registerEvents({
      downNode: (event) => {
        const { node } = event;
        setDraggedNode(node);
      },
      mousemove: (event) => {
        if (!draggedNode) return;

        const pos = sigma.viewportToGraph(event);

        sigma.getGraph().setNodeAttribute(draggedNode, "x", pos.x);
        sigma.getGraph().setNodeAttribute(draggedNode, "y", pos.y);
        event.preventSigmaDefault();
        event.original.preventDefault();
        event.original.stopPropagation();
        sigma.refresh();
      },
      mouseup: (event) => {
        const pos = sigma.viewportToGraph(event);

        if (draggedNode !== null) {
          setGraphData((prevData) => {
            const updatedNodes = prevData.nodes.map((node) =>
              node.id === draggedNode ? { ...node, x: pos.x, y: pos.y } : node
            );
            return { ...prevData, nodes: updatedNodes };
          });
          event.original.preventDefault();
          event.original.stopPropagation();
          setDraggedNode(null);
        } else {
          addNode(pos.x, pos.y);
        }
      },
    });
  }, [registerEvents, sigma, draggedNode]);

  return (
    <div>
      <button onClick={saveGraph} style={{ position: "absolute", zIndex: 1 }}>
        Save Layout
      </button>
    </div>
  );
};

interface DisplayGraphProps {
  initialGraphData: GraphData;
}

export const DisplayGraph = ({initialGraphData}: DisplayGraphProps) => {
  return (
    <SigmaContainer
      style={sigmaStyle}
      graph={MultiDirectedGraph}
      settings={{
        autoRescale: true,
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
