<p align="center">
  <img src="logo.svg" alt="Flumen Logo" width="350"/>
</p>

**Flumen** is a real-time visualization engine for tracking moving objects across a dynamic graph. It's designed to receive positional data from an external system (like a factory PLC or logistics API) and render the movement in an interactive, user-configurable interface.

Imagine watching items flow along a complex conveyor belt system, seeing their status change in real-time, and being able to replay the data. That's the goal of Flumen.

> ⚠️ **Work in Progress**
>
> This project is currently under active development. The core backend functionalities are nearing completion, but APIs are subject to change and features are still being added.

---

## 💡 Core Concepts

Flumen's design is based on a few key principles:

### 1. Node-Centric Tracks
Unlike traditional graph visualizations where edges represent the path, in Flumen, **specialized `Location` nodes represent the actual tracks**, this helps in tracking the exact item position and progress on the section.
- **Nodes** can be simple points (e.g., a sensor) or tracks (`Location` nodes).
- **`Location` nodes** have properties like `length` and `speed`. When an item enters a `Location` node, the frontend animates its movement along that node's length at the specified speed.
- **Edges** simply define the directed flow, indicating the *next* node an item will move to.

### 2. Decoupled State: The Reconciliation Loop
Flumen cleanly separates the "source of truth" from the visualization.
1.  **External System:** The authoritative source that knows the true location of every item.
2.  **Flumen Backend:** Listens for events from the external system (e.g., "Item A has arrived at Location 2"). It maintains the official state.
3.  **Flumen Frontend:** When the backend confirms an item is on a `Location` node, the frontend begins an "optimistic" animation of the item moving along that node. This provides a smooth visual experience without needing constant updates. The item officially moves to the *next* node only when the backend receives another confirmation event.

### 3. Event Sourcing for History and Auditing
The entire system is built on an **[Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)** architecture. Instead of just storing the current state, we store an immutable sequence of all events that have ever happened (e.g., `NodeCreated`, `ItemMoved`, `NodeAttributeChanged`).
- **Full History:** Replay events to see the state of the system at any point in time.
- **Debugging & Auditing:** Provides a complete, unchangeable log of every action.
- **State Resilience:** If a process stops, the item's progress (e.g., percentage traveled along a `Location` node) can be precisely stored and resumed.

## ✨ Key Features

- **Real-time Visualization:** See objects move and states change instantly via WebSockets.
- **Dynamic Graph Editor:** Create, move, and configure nodes and their connections directly in the UI.
- **Stateful Animations:** The frontend animates object movement based on the `length` and `speed` attributes of `Location` nodes.
- **Status-Driven Visuals:** Nodes dynamically change their appearance (e.g., color) to reflect their state (`ok`, `alarm`, `inactive`).
- **Event-Sourced Backend:** A robust architecture that provides a full, replayable history of the system.

## 🛠️ Architecture

The project follows a clean separation of concerns:
- **`Domain`:** Contains all the core business logic, rules, and the event sourcing implementation. It is completely independent of any framework.
- **`Models`:** Data Transfer Objects (DTOs) used for defining the shape of API inputs and outputs.
- **`Controllers`:** The API layer that handles HTTP requests, validates input, and orchestrates calls to the domain logic.

## 🚀 Roadmap: The Vision for Flumen

Here is a look at the planned features and improvements to make Flumen a comprehensive and powerful tool.

### 📊 Visualization & Performance
- [ ] **WebGL Rendering:** Migrate the renderer to WebGL (e.g., using `pixi.js` or `react-three-fiber`) for buttery-smooth performance with thousands of nodes.
- [ ] **Node Clustering & LOD:** Automatically group dense nodes and implement Level-of-Detail rendering to maintain performance on large graphs.

### 🧠 State Management & Prediction
- [ ] **Predictive Movement:** Develop a "ghost" mode to show the most likely future path of an object.
- [ ] **State Time-Travel:** Leverage the event-sourced history to create a "timeline" slider for debugging and reviewing past incidents.
- [ ] **Web Workers:** Move physics and layout calculations to Web Workers to ensure a non-blocking UI.

### 🧑‍💻 User Experience & Interactivity
- [ ] **Advanced Monitoring:** Create a central dashboard for system-wide metrics, alerts, and traffic heat maps.
- [ ] **Pathfinding Tools:** Allow users to select two nodes and highlight the shortest or most efficient path.
- [ ] **Advanced Filtering & Search:** Implement a powerful search to quickly find nodes or items by their attributes.

### 🏗️ Architecture & Scalability
- [ ] **Plugin & Extension System:** Design a plugin architecture to allow for custom node types, movement algorithms, and external API integrations.
- [ ] **Multi-Source Data Ingestion:** Add protocol adapters to ingest real-time data from various sources like **MQTT**, **AMQP**, or **Kafka**.
- [ ] **Historical Data Archiving:** Implement a strategy for archiving old events to a cold storage solution to keep the primary database fast.
