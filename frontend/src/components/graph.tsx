import React, { useEffect } from "react";
import { SigmaContainer, useSigma, useLoadGraph, ControlsContainer, ZoomControl, FullScreenControl, useRegisterEvents } from "@react-sigma/core";
import { MultiDirectedGraph } from "graphology";
import { NodeSquareProgram } from "@sigma/node-square";
import "@react-sigma/core/lib/react-sigma.min.css";
import { useState } from "react";

const sigmaStyle = { height: "100vh", width: "100vw" };

const LoadGraph = () => {
  const sigma = useSigma();
  const graph = new MultiDirectedGraph();
  const loadGraph = useLoadGraph();
  const [draggedNode, setDraggedNode] = useState(null);

  const [graphData, setGraphData] = useState({
    nodes: [
      { id: 'A', x: -0.5, y: -0.5 },
      { id: 'B', x: 0.5, y: -0.5 },
      { id: 'C', x: 0.5, y: 0.5 },
      { id: 'D', x: -0.5, y: 0.5 },
    ],
    edges: [
      { source: 'A', target: 'B' },
      { source: 'B', target: 'C' },
      { source: 'C', target: 'D' },
      { source: 'D', target: 'A' },
    ],
  });  

  graphData.nodes.forEach(({ id, x, y }) => {
    graph.addNode(id, {
      label: `Node ${id}`,
      x,
      y,
      size: 10,
      fixed: true,
    });
  });

  graphData.edges.forEach((edge) => {
    graph.addEdge(edge.source, edge.target);
  })

  graph.addNode("Item", {
    label: "Item",
    x: 0,
    y: 0,
    size: 8,
    color: "#FF0000",
    type: "square",
  });

  useEffect(() => {
    loadGraph(graph);

    const handleMouseDown = (event: { node: any; }) => {

      const { node } = event;
      setDraggedNode(node);
    };

    const handleMouseMove = (event: any) => {
      if (!draggedNode) return;

      const pos = sigma.viewportToGraph(event);

      graph.setNodeAttribute(draggedNode, "x", pos.x);
      graph.setNodeAttribute(draggedNode, "y", pos.y);
      event.preventSigmaDefault();
      event.original.preventDefault();
      event.original.stopPropagation();
      sigma.refresh();
    };

    const handleMouseUp = (event: any) => {

      const pos = sigma.viewportToGraph(event);

      if (draggedNode !== null) {
        setGraphData((prevData) => {
          const updatedNodes = prevData.nodes.map((node) =>
            node.id === draggedNode ? { ...node, x: pos.x, y: pos.y } : node
          );
          return { ...prevData, nodes: updatedNodes };
        });
        event.original.preventDefault();
        event.original.stopPropagation()
        setDraggedNode(null);
      }else{
        addNode(pos.x, pos.y)
      }
    };

    sigma.on("downNode", handleMouseDown);
    sigma.getMouseCaptor().on("mousemove", handleMouseMove);
    sigma.getMouseCaptor().on("mouseup", handleMouseUp);

    // Animation function to move the item along the edges
    let t = 0;
    const speed = 0.01; // Adjust speed as needed
    const path = ["A", "B", "C", "D", "A"]; // Define the path

    const animate = () => {
      const segment = Math.floor(t) % (path.length - 1);
      const startNode = path[segment];
      const endNode = path[segment + 1];

      const startX = graph.getNodeAttribute(startNode, "x");
      const startY = graph.getNodeAttribute(startNode, "y");
      const endX = graph.getNodeAttribute(endNode, "x");
      const endY = graph.getNodeAttribute(endNode, "y");

      const progress = t - Math.floor(t);

      const newX = startX + (endX - startX) * progress;
      const newY = startY + (endY - startY) * progress;

      graph.setNodeAttribute("Item", "x", newX);
      graph.setNodeAttribute("Item", "y", newY);

      sigma.refresh();

      t += speed;
      requestAnimationFrame(animate);
    };

    animate();
    return () => {
      sigma.off("downNode", handleMouseDown);
      sigma.getMouseCaptor().removeListener("mousemove", handleMouseMove);
      sigma.getMouseCaptor().removeListener("mouseup", handleMouseUp);
    };
  }, [sigma, graph, loadGraph, draggedNode]);

  const saveGraph = () => {

    localStorage.setItem("graphPositions", JSON.stringify(graphData));
    console.log("SAVED", graphData)
    alert("Graph layout saved!");
  };

  const addNode = (x, y) => {
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

  // useRegisterEvents({
  //   clickStage: ({ event }) => {
  //     const { x, y } = event;
  //     addNode(x, y);
  //   },
  // });

    return (
    <div>
      <button onClick={saveGraph} style={{ position: "absolute", zIndex: 1 }}>
        Save Layout
      </button>
    </div>
  );
};

export const DisplayGraph = () => {
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
      <LoadGraph />
      <ControlsContainer position={"bottom-right"}>
      <ZoomControl />
      <FullScreenControl />
    </ControlsContainer>
    </SigmaContainer>
  );
};