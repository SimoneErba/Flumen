import React, { useEffect, useState, useRef, useCallback } from "react";
import * as graphologyMetrics from "graphology-metrics"; // This is correct
import {
  SigmaContainer,
  useSigma,
  useLoadGraph,
  ControlsContainer,
  ZoomControl,
  FullScreenControl,
  EdgeReducer,
  useRegisterEvents
} from "@react-sigma/core";
import { MultiDirectedGraph } from "graphology";
import { NodeSquareProgram } from "@sigma/node-square";
import "@react-sigma/core/lib/react-sigma.min.css";

const hashToNumber = (s: string) => {
  let hash = 0;
  for (let i = 0; i < s.length; i++) {
    const char = s.charCodeAt(i);
    hash = (hash << 5) - hash + char;
    hash = hash & hash; // Convert to 32bit integer
  }
  return Math.abs(hash);
};

export interface GraphData {
  locations: Array<{
    id: string;
    name: string;
    longitude?: number;
    latitude?: number;
    speed: number;
    length: number;
    items?: Array<{ id: string; name: string }>;
  }>;
  connections: Array<{
    sourceId: string;
    targetId: string;
  }>;
}
// --- End of Stubs ---


const sigmaStyle: React.CSSProperties = { height: "100vh", width: "100vw" };

// --- Edge Editor Component ---
// --- Edge Editor Component ---
interface EdgeEditorData {
  edgeId: string;
  sourceId: string;
  targetId: string;
  speed: number;
  length: number;
}

interface EdgeEditorProps {
  data: EdgeEditorData;
  onClose: () => void;
  onSubmit: (updatedData: { speed: number; length: number; isReversed: boolean }) => void;
  onDelete: (edgeId: string) => void;
}

const EdgeEditor = ({ data, onClose, onSubmit, onDelete }: EdgeEditorProps) => {
  const [speed, setSpeed] = useState(data.speed);
  const [length, setLength] = useState(data.length);
  const [isReversed, setIsReversed] = useState(false);

  const handleSubmit = () => {
    onSubmit({ speed: Number(speed), length: Number(length), isReversed });
    onClose();
  };

  const handleDelete = () => {
    if (window.confirm(`Are you sure you want to delete the path from ${data.sourceId} to ${data.targetId}?`)) {
      onDelete(data.edgeId);
      onClose();
    }
  };

  const currentSourceLabel = isReversed ? data.targetId : data.sourceId;
  const currentTargetLabel = isReversed ? data.sourceId : data.targetId;

  return (
    <div style={editorStyle}>
      <h4>Edit Path</h4>
      <p><b>From:</b> {currentSourceLabel}</p>
      <p><b>To:</b> {currentTargetLabel}</p>
      
      <label>Speed:</label>
      <input type="number" value={speed} onChange={e => setSpeed(Number(e.target.value))} />
      
      <label>Length:</label>
      <input type="number" value={length} onChange={e => setLength(Number(e.target.value))} />
      
      <div style={buttonGroupStyle}>
        <button onClick={() => setIsReversed(!isReversed)}>
          {isReversed ? 'Direction Reversed' : 'Reverse Direction'}
        </button>
      </div>

      <div style={iconGroupStyle}>
        <button onClick={handleDelete} title="Delete">🗑️</button>
        <button onClick={onClose} title="Close">❌</button>
        <button onClick={handleSubmit} title="Submit">✔️</button>
      </div>
    </div>
  );
};

const editorStyle: React.CSSProperties = {
  position: 'absolute', top: '10px', right: '10px', background: 'white',
  padding: '10px', border: '1px solid #ccc', borderRadius: '8px',
  boxShadow: '0 2px 10px rgba(0,0,0,0.1)', zIndex: 110, // Increased z-index
  display: 'flex', flexDirection: 'column', gap: '8px'
};
const buttonGroupStyle: React.CSSProperties = { display: 'flex', justifyContent: 'center', marginTop: '10px' };
const iconGroupStyle: React.CSSProperties = { display: 'flex', justifyContent: 'space-between', marginTop: '10px' };

// --- Graph Logic Component ---
interface AnimationState {
  sourceId: string;
  targetId: string;
  startTime: number;
  duration: number;
}

// *** NEW ***: Interface for the visual feedback line's coordinates
interface LineCoordinates {
  x1: number;
  y1: number;
  x2: number;
  y2: number;
}

interface GraphEventsProps {
  initialGraphData: GraphData;
  setHoveredEdge: (edge: string | null) => void;
}

const GraphEvents = ({ initialGraphData, setHoveredEdge }: GraphEventsProps) => {
  const sigma = useSigma();
  const registerEvents = useRegisterEvents();
  const loadGraph = useLoadGraph();

  const draggedNodeRef = useRef<string | null>(null);
  const isDraggingRef = useRef<boolean>(false);
  const [selectedEdgeData, setSelectedEdgeData] = useState<EdgeEditorData | null>(null);
  
  const [animatingItems, setAnimatingItems] = useState<Record<string, AnimationState>>({});
  const animatingItemsRef = useRef<Record<string, AnimationState>>(animatingItems);
  animatingItemsRef.current = animatingItems;
  const animationFrameId = useRef<number | null>(null);

  const selectedEdgeRef = useRef(selectedEdgeData);
  selectedEdgeRef.current = selectedEdgeData;
  
  // *** NEW ***: State and refs for adding a new edge
  const [lineCoordinates, setLineCoordinates] = useState<LineCoordinates | null>(null);
  const isAddingEdgeRef = useRef<boolean>(false);
  const edgeSourceNodeRef = useRef<string | null>(null);


  useEffect(() => {
    const graph = new MultiDirectedGraph();
    initialGraphData.locations.forEach(location => {
      graph.addNode(location.id, {
        x: location.longitude ?? hashToNumber(location.id),
        y: location.latitude ?? hashToNumber(location.id + "random"),
        label: location.name, size: 10, color: "#69b3a2",
        speed: location.speed, length: location.length,
      });
    });
    initialGraphData.locations.forEach(location => {
      (location.items || []).forEach(item => {
        graph.addNode(item.id, {
          x: location.longitude ?? hashToNumber(location.id),
          y: location.latitude ?? hashToNumber(location.id + "random"),
          label: item.name, size: 8, color: "#FF0000", type: "square",
          initialLocationId: location.id,
        });
      });
    });
    initialGraphData.connections.forEach(c => {
      if (graph.hasNode(c.sourceId) && graph.hasNode(c.targetId)) {
        graph.addEdge(c.sourceId, c.targetId, { type: 'arrow', size: 5 });
      }
    });
    loadGraph(graph);
    const initialAnimations: Record<string, AnimationState> = {};
    graph.forEachNode((node, attrs) => {
      if (attrs.type === "square") {
        const sourceLocAttrs = graph.getNodeAttributes(attrs.initialLocationId);
        const nextTargets = graph.outEdges(attrs.initialLocationId).map(edge => graph.target(edge));
        const uniqueTargets = [...new Set(nextTargets)];
        if (sourceLocAttrs && sourceLocAttrs.speed > 0 && uniqueTargets.length === 1) {
          const targetId = uniqueTargets[0];
          const duration = (sourceLocAttrs.length / sourceLocAttrs.speed) * 1000;
          initialAnimations[node] = { sourceId: attrs.initialLocationId, targetId, startTime: Date.now(), duration };
        }
      }
    });
    setAnimatingItems(initialAnimations);
  }, [initialGraphData, loadGraph]);

  useEffect(() => {
    const animate = () => {
      const graph = sigma.getGraph();
      if (!graph) {
        animationFrameId.current = requestAnimationFrame(animate);
        return;
      }
      const currentTime = Date.now();
      let needsRefresh = false;
      let itemsHaveChanged = false;
      const nextAnimatingItems = { ...animatingItemsRef.current };
      for (const itemId in animatingItemsRef.current) {
        if (itemId === draggedNodeRef.current) continue;
        const anim = animatingItemsRef.current[itemId];
        const sourceNode = graph.getNodeAttributes(anim.sourceId);
        const targetNode = graph.getNodeAttributes(anim.targetId);
        if (!sourceNode || !targetNode) continue;
        const progress = Math.min((currentTime - anim.startTime) / anim.duration, 1);
        graph.setNodeAttribute(itemId, "x", sourceNode.x + (targetNode.x - sourceNode.x) * progress);
        graph.setNodeAttribute(itemId, "y", sourceNode.y + (targetNode.y - sourceNode.y) * progress);
        needsRefresh = true;
        if (progress >= 1) {
          itemsHaveChanged = true;
          const newSourceId = anim.targetId;
          const newSourceLocationAttrs = graph.getNodeAttributes(newSourceId);
          const nextPossibleTargets = graph.outEdges(newSourceId).map(edge => graph.target(edge));
          const uniqueNextTargets = [...new Set(nextPossibleTargets)];
          if (uniqueNextTargets.length === 1 && newSourceLocationAttrs && newSourceLocationAttrs.speed > 0) {
            const newTargetId = uniqueNextTargets[0];
            const duration = (newSourceLocationAttrs.length / newSourceLocationAttrs.speed) * 1000;
            nextAnimatingItems[itemId] = { sourceId: newSourceId, targetId: newTargetId, startTime: Date.now(), duration };
          } else {
            delete nextAnimatingItems[itemId];
          }
        }
      }
      if (needsRefresh) sigma.refresh();
      if (itemsHaveChanged) {
        setAnimatingItems(nextAnimatingItems);
      }
      animationFrameId.current = requestAnimationFrame(animate);
    };
    animationFrameId.current = requestAnimationFrame(animate);
    return () => {
      if (animationFrameId.current) cancelAnimationFrame(animationFrameId.current);
    };
  }, [sigma]);

  const handleEdgeSubmit = useCallback(({ speed, length, isReversed }: { speed: number; length: number; isReversed: boolean }) => {
    if (!selectedEdgeData) return;
    const graph = sigma.getGraph();
    const { sourceId, targetId, edgeId } = selectedEdgeData;
    const sourceOfEdit = isReversed ? targetId : sourceId;
    graph.setNodeAttribute(sourceOfEdit, 'speed', speed);
    graph.setNodeAttribute(sourceOfEdit, 'length', length);
    if (isReversed) {
      if (graph.hasEdge(edgeId)) {
        graph.dropEdge(edgeId);
        graph.addEdge(targetId, sourceId, { type: 'arrow', size: 5 });
      }
    }
    sigma.refresh();
  }, [sigma, selectedEdgeData]);

  const handleEdgeDelete = useCallback((edgeId: string) => {
    const graph = sigma.getGraph();
    if (graph.hasEdge(edgeId)) {
      graph.dropEdge(edgeId);
      sigma.refresh();
    }
  }, [sigma]);

  const addNode = useCallback((x: number, y: number) => {
    const graph = sigma.getGraph();
    if (!graph) return;
    const newNodeId = crypto.randomUUID();
    const closestNodes = graph.nodes()
      .filter(nodeId => !graph.getNodeAttribute(nodeId, "type"))
      .map(nodeId => {
        const attrs = graph.getNodeAttributes(nodeId);
        return { nodeId, distance: Math.pow(x - attrs.x, 2) + Math.pow(y - attrs.y, 2) };
      })
      .sort((a, b) => a.distance - b.distance)
      .slice(0, 2);
    graph.addNode(newNodeId, { x, y, label: "New Location", size: 10, color: "#69b3a2", speed: 10, length: 50 });
    closestNodes.forEach(e => {
      graph.addEdge(e.nodeId, newNodeId, { type: 'arrow', size: 5 });
      // To make it bidirectional by default, add the reverse edge too
      // graph.addEdge(newNodeId, e.nodeId, { type: 'arrow', size: 5 });
    });
  }, [sigma]);

  // *** MODIFIED ***: EVENT REGISTRATION (WITH ALT+DRAG FOR EDGES)
  useEffect(() => {
    registerEvents({
      enterEdge: ({ edge }) => setHoveredEdge(edge),
      leaveEdge: () => setHoveredEdge(null),
      clickEdge: ({ edge }) => {
        const graph = sigma.getGraph();
        if (!graph.hasEdge(edge)) return;
        const sourceId = graph.source(edge);
        const targetId = graph.target(edge);
        const sourceAttrs = graph.getNodeAttributes(sourceId);
        setSelectedEdgeData({
          edgeId: edge, sourceId, targetId,
          speed: sourceAttrs.speed || 0, length: sourceAttrs.length || 0,
        });
      },
      clickStage: ({ event }) => {
        if (selectedEdgeRef.current) {
          setSelectedEdgeData(null);
        } else if (!isDraggingRef.current) {
          const pos = sigma.viewportToGraph(event);
          addNode(pos.x, pos.y);
        }
      },
      downNode: ({ node, event }) => {
        // Check for Alt key to decide action
        if (event.original.altKey) {
          // --- STARTING TO ADD AN EDGE ---
          event.preventSigmaDefault();
          isAddingEdgeRef.current = true;
          edgeSourceNodeRef.current = node;
          const nodeDisplayData = sigma.getNodeDisplayData(node);
          if (nodeDisplayData) {
            setLineCoordinates({
              x1: nodeDisplayData.x, y1: nodeDisplayData.y,
              x2: nodeDisplayData.x, y2: nodeDisplayData.y,
            });
          }
        } else {
          // --- STARTING TO MOVE A NODE (existing logic) ---
          setSelectedEdgeData(null);
          if (sigma.getGraph().getNodeAttribute(node, "type") !== "square") {
            isDraggingRef.current = true;
            draggedNodeRef.current = node;
            sigma.getSettings().mouseEnabled = false;
          }
        }
      },
      upNode: ({ node }) => {
        // Check if we were in "add edge" mode and dropped on a different, valid node
        if (isAddingEdgeRef.current && edgeSourceNodeRef.current && edgeSourceNodeRef.current !== node) {
          const graph = sigma.getGraph();
          const source = edgeSourceNodeRef.current;
          const target = node;
          if (!graph.hasEdge(source, target)) {
            graph.addEdge(source, target, { type: 'arrow', size: 5 });
          }
        }
      },
      mouseup: () => {
        // --- CLEANUP FOR ALL ACTIONS ---
        if (isAddingEdgeRef.current) {
          isAddingEdgeRef.current = false;
          edgeSourceNodeRef.current = null;
          setLineCoordinates(null); // Hide the feedback line
        }
        if (isDraggingRef.current) {
          isDraggingRef.current = false;
          draggedNodeRef.current = null;
          sigma.getSettings().mouseEnabled = true;
        }
      },
      mousemove: (event) => {
        // Handle moving a node (this part is fine)
        if (isDraggingRef.current && draggedNodeRef.current) {
          event.preventSigmaDefault();
          const pos = sigma.viewportToGraph(event);
          sigma.getGraph().setNodeAttribute(draggedNodeRef.current, "x", pos.x);
          sigma.getGraph().setNodeAttribute(draggedNodeRef.current, "y", pos.y);
          return; // Exit early
        }

        // Handle drawing the edge line
        if (isAddingEdgeRef.current && edgeSourceNodeRef.current) {
          event.preventSigmaDefault();
           // Get the source node's *current* position from sigma, not from a stale state
          const sourceNodeDisplayData = sigma.getNodeDisplayData(edgeSourceNodeRef.current);
          if (!sourceNodeDisplayData) return;

          // The 'event' object contains the current mouse coordinates
          // No need to use a stale 'lineCoordinates' from a previous render
          const { x: x2, y: y2 } = event; // These are viewport coordinates

          setLineCoordinates({
            x1: sourceNodeDisplayData.x,
            y1: sourceNodeDisplayData.y,
            x2: x2, // Use live coordinate from the event
            y2: y2, // Use live coordinate from the event
          });
        }
      },
    });
  // This stable dependency array is key to preventing bugs
  }, [sigma, registerEvents, addNode, setHoveredEdge]);

  return (
    <>
      {/* *** NEW ***: SVG Overlay for visual feedback when adding an edge */}
      <svg
        style={{
          position: 'absolute', top: 0, left: 0,
          width: '100%', height: '100%',
          pointerEvents: 'none', // Allows clicks to pass through to the graph
          zIndex: 100,
        }}
      >
        {lineCoordinates && (
          <line
            x1={lineCoordinates.x1} y1={lineCoordinates.y1}
            x2={lineCoordinates.x2} y2={lineCoordinates.y2}
            stroke="#ff5500" strokeWidth="2"
          />
        )}
      </svg>
      {console.log(lineCoordinates)}
    
      {selectedEdgeData && (
        <EdgeEditor
          data={selectedEdgeData}
          onSubmit={handleEdgeSubmit}
          onClose={() => setSelectedEdgeData(null)}
          onDelete={handleEdgeDelete}
        />
      )}
      <ControlsContainer position={"bottom-right"}>
        <ZoomControl />
        <FullScreenControl />
      </ControlsContainer>
    </>
  );
};

// --- Main Display Component ---
export const DisplayGraph = ({ initialGraphData }: { initialGraphData: GraphData }) => {
  const [hoveredEdge, setHoveredEdge] = useState<string | null>(null);

  const edgeReducer = useCallback<EdgeReducer>((edge, attrs) => {
    if (hoveredEdge === edge) {
      return { ...attrs, color: "#ff5500", size: 7 };
    }
    return attrs;
  }, [hoveredEdge]);

  return (
    <div style={{ width: '100%', height: '100vh', position: 'relative' }}>
    <SigmaContainer
      style={{ ...sigmaStyle, cursor: hoveredEdge ? 'pointer' : 'default' }}
      settings={{
        nodeProgramClasses: { square: NodeSquareProgram },
        enableEdgeEvents: true,
        autoRescale: true
      }}
      edgeReducer={edgeReducer}
    >
      <GraphEvents initialGraphData={initialGraphData} setHoveredEdge={setHoveredEdge} />
    </SigmaContainer>
    </div>
  );
};

export default DisplayGraph;