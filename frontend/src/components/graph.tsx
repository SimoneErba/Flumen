import React, { useEffect } from "react";
import { SigmaContainer, useSigma, useLoadGraph } from "@react-sigma/core";
import { MultiDirectedGraph } from "graphology";
import { NodeSquareProgram } from "@sigma/node-square";
import "@react-sigma/core/lib/react-sigma.min.css";

const sigmaStyle = { height: "100vh", width: "100vw" };

const LoadGraph = () => {
  const sigma = useSigma();
  const graph = new MultiDirectedGraph();
  const loadGraph = useLoadGraph();

  // Define fixed nodes with specific positions
  const fixedNodes = [
    { id: "A", x: 0, y: 0 },
    { id: "B", x: 1, y: 0 },
    { id: "C", x: 1, y: 1 },
    { id: "D", x: 0, y: 1 },
  ];

  // Add fixed nodes to the graph
  fixedNodes.forEach(({ id, x, y }) => {
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
  }, [graph, loadGraph, sigma]);

  return null;
};

export const DisplayGraph = () => {
  return (
    <SigmaContainer
      style={sigmaStyle}
      graph={MultiDirectedGraph}
      settings={{
        nodeProgramClasses: {
          square: NodeSquareProgram, // Register the square node renderer
        },
      }}
    >
      <LoadGraph />
    </SigmaContainer>
  );
};