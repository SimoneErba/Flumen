import React, { useEffect } from "react";
import { SigmaContainer, useSigma, useLoadGraph, ControlsContainer, ZoomControl, FullScreenControl } from "@react-sigma/core";
import { MultiDirectedGraph } from "graphology";
import { NodeSquareProgram } from "@sigma/node-square";
import "@react-sigma/core/lib/react-sigma.min.css";
import { useState } from "react";

const sigmaStyle = { height: "100vh", width: "100vw" };

const LoadGraph = () => {
  const sigma = useSigma();
  const graph = new MultiDirectedGraph();
  const loadGraph = useLoadGraph();
  const [nodePositions, setNodePositions] = useState({});
  const [draggedNode, setDraggedNode] = useState(null);
  // Define fixed nodes with specific positions
  const fixedNodes = [
    { id: "A", x: 0, y: 0 },
    { id: "B", x: 1, y: 0 },
    { id: "C", x: 1, y: 1 },
    { id: "D", x: 0, y: 1 },
  ];

  const updatedNodes = fixedNodes.map(node => {
    const position = nodePositions[node.id];
    return position ? { ...node, x: position.x, y: position.y } : node;
  });
  

  // Add fixed nodes to the graph
  updatedNodes.forEach(({ id, x, y }) => {
    graph.addNode(id, {
      label: `Node ${id}`,
      x,
      y,
      size: 10,
      fixed: true,
    });
  });

  // Add edges between nodes
  graph.addEdge("A", "B");
  graph.addEdge("B", "C");
  graph.addEdge("C", "D");
  graph.addEdge("D", "A");

  // Add an item node (square) starting at node A
  graph.addNode("Item", {
    label: "Item",
    x: 0,
    y: 0,
    size: 8,
    color: "#FF0000",
    type: "square", // Custom attribute to identify square nodes
  });

  useEffect(() => {
    // Load the graph into Sigma
    loadGraph(graph);

    const handleMouseDown = (event) => {

      const { node } = event;
      setDraggedNode(node);

      const initialPosition = { x: graph.getNodeAttribute(node, "x"), y: graph.getNodeAttribute(node, "y") };
      // setNodePositions((prevPositions) => ({
      //   ...prevPositions,
      //   [node]: initialPosition,
      // }));
    };

    const handleMouseMove = (event) => {
      if (!draggedNode) return;

      const pos = sigma.viewportToGraph(event);

      graph.setNodeAttribute(draggedNode, "x", pos.x);
      graph.setNodeAttribute(draggedNode, "y", pos.y);
      // setNodePositions((prevPositions) => ({
      //   ...prevPositions,
      //   [draggedNode]: { x: pos.x, y: pos.y },
      // }));
      event.preventSigmaDefault();
      event.original.preventDefault();
      event.original.stopPropagation();
      sigma.refresh();
    };

    const handleMouseUp = (event) => {

      const pos = sigma.viewportToGraph(event);

      if (draggedNode !== null) {
        setNodePositions((prevPositions) => ({
          ...prevPositions,
          [draggedNode]: { x: pos.x, y: pos.y },
        }));
        setDraggedNode(null);
      }
      // setNodePositions({});
    };

    sigma.on("downNode", handleMouseDown);
    sigma.getMouseCaptor().on("mousemove", handleMouseMove);
    sigma.getMouseCaptor().on("mouseup", handleMouseUp);
    sigma.getMouseCaptor().on("mousedown", handleMouseUp);
    
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
      sigma.getMouseCaptor().removeListener("mousedown", handleMouseUp);
    };
  }, [sigma, graph, loadGraph, draggedNode]);

  const saveGraph = () => {
    const positions = {};
    graph.forEachNode((node, attributes) => {
      positions[node] = { x: attributes.x, y: attributes.y };
    });
    // Save positions to local storage or send to backend
    localStorage.setItem("graphPositions", JSON.stringify(positions));
    alert("Graph layout saved!");
  };

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