# Flumen

The idea is that nodes move over nodes. edges are just used to indicate whats the next node, but actual connections are represented by nodes. Nodes with type "location" that can have a length and a speed. if they have length and speed, then when an item goes over them, it starts to move. (not in the backedn, only in the frontend). Then , when it reaches the end, it will move to the next location. but we wait for the external system to notify us. for us, it will stay in the old location. if a location gets inactive, it will stop moving on the frontend. if it changes type "alarm, ok", it will change color.

nodes can be created in the frontend and users can change their position and attributes. clicking on an edge will update the location node. 

### Models

entitys are not used are just to rememebr the possible fields.
models are used as input anbd output if the controllers
domain have the logic

### What I decided

https://martinfowler.com/eaaDev/EventSourcing.html


edges store the progress of the item in case of stops. (the length done). just a percetage with a datetime and if history is needed, evwnt sourcing


## Future Improvements

### 1. Visualization and Performance
- **Graph Layout Optimization**
  - Implement force-directed layouts for automatic node organization
  - Add multiple layout algorithms (hierarchical, circular)
  - WebGL rendering support for better performance
- **Real-time Performance**
  - Node clustering for dense areas
  - Level-of-detail rendering
  - Web Workers for non-blocking calculations

### 2. State Management and Prediction
- **Predictive Movement**
  - Basic prediction system for likely movement paths
  - Ghost previews of future positions
  - Confidence indicators for predictions
- **State Handling**
  - State rollback/forward for debugging
  - Optimized state diffing
  - State persistence and recovery

### 3. User Experience
- **Interactive Features**
  - Path planning tools
  - Timeline view for historical movements
  - Advanced filtering and search
- **Monitoring and Alerts**
  - Configurable condition alerts
  - System-wide metrics dashboard
  - Heat maps for traffic analysis

### 4. Architecture and Scalability
- **Backend Optimization**
  - Path caching
  - Batch updates
  - WebSocket compression
- **Data Management**
  - Hierarchical location grouping
  - Historical data archiving
  - System configuration import/export

### 5. Documentation and Testing
- **Documentation**
  - Interactive examples and demos
  - Comprehensive API documentation
  - Performance guidelines
- **Testing**
  - Load testing for scalability
  - Visual regression testing
  - Simulation tools

### 6. Integration and Extensibility
- **API Extensions**
  - Plugin system for custom node types
  - Custom movement algorithm hooks
  - External integration event system
- **Data Sources**
  - Multi-source real-time data ingestion
  - Protocol adapters (MQTT, AMQP)
  - Custom data transformations

### 7. Analytics and Reporting
- **Movement Analytics**
  - Path efficiency analysis
  - Congestion detection
  - Custom metrics support
- **Reporting**
  - Automated report generation
  - Multiple export formats
  - Customizable dashboards

These improvements aim to enhance the system's functionality, performance, and user experience while maintaining its core concept of node-based movement visualization. 