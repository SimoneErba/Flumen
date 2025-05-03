package com.livedatatrail.backend.services;

import com.livedatatrail.backend.domain.Item;
import com.livedatatrail.backend.domain.events.*;
import com.livedatatrail.backend.models.UpdateModel;
import com.livedatatrail.backend.models.input.ItemInput;
import com.livedatatrail.backend.utils.OrientDBUtils;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.id.ORID;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ItemService {
    private final EventStore eventStore;
    private final OrientDBService orientDBService;
    private final ItemEventProcessor eventProcessor;
    private final UpdateService updateService;
    public ItemService(EventStore eventStore, OrientDBService orientDBService, ItemEventProcessor eventProcessor, UpdateService updateService) {
        this.eventStore = eventStore;
        this.orientDBService = orientDBService;
        this.eventProcessor = eventProcessor;
        this.updateService = updateService;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        try (ODatabaseSession db = orientDBService.getSession()) {
            try (OResultSet rs = db.query("SELECT * FROM Item")) {
                while (rs.hasNext()) {
                    OResult row = rs.next();
                    row.getVertex().ifPresent(vertex -> {
                        Item item = vertexToItem(vertex);
                        items.add(item);
                    });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching items: " + e.getMessage(), e);
        }
    
        return items;
    }
    

    public Item getItemById(String id) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            var itemInDb = OrientDBUtils.loadAndValidateVertexByCustomId(db, id);
            Item item = vertexToItem(itemInDb);
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching item with ID " + id + ": " + e.getMessage(), e);
        }
    }

    public Item createItem(Item item) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            if (OrientDBUtils.checkIfAlreadyExists(db, item.getId())) {
                throw new IllegalArgumentException("Item with ID " + item.getId() + " already exists.");
            }
    
            OVertex vertex = db.newVertex("Item");
            vertex.setProperty("customId", item.getId());
            vertex.setProperty("name", item.getName());
            vertex.setProperty("speed", item.getSpeed());
            vertex.setProperty("active", item.isActive());
            vertex.setProperty("properties", item.getProperties());
    
            vertex.save();
            return vertexToItem(vertex);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while creating item with ID " + item.getId() + ": " + e.getMessage(), e);
        }
    }
    

    public Item updateItem(UpdateModel model) {
        return  vertexToItem(this.updateService.updateVertex(model));
    }

    public Item fullUpdateItem(Item item) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            OVertex vertex = OrientDBUtils.loadAndValidateVertexByCustomId(db, item.getId());
            vertex.setProperty("name", item.getName());
            vertex.setProperty("speed", item.getSpeed());
            vertex.setProperty("active", item.isActive());
            vertex.setProperty("properties", item.getProperties());
            vertex.save();

            if (item.getLocation() != null) {
                String newLocationId = item.getLocation().getId();

                OEdge edge = null;
                if (newLocationId != item.getLocation().getId()) {
                    // Remove all existing HasPosition edges
                    Iterator<OEdge> edges = vertex.getEdges(ODirection.OUT, "HasPosition").iterator();
                    while (edges.hasNext()) {
                        edges.next().delete();
                    }

                    // Add new HasPosition edge to the new location
                    OVertex newLocation = OrientDBUtils.loadAndValidateVertexByCustomId(db, newLocationId);
                    edge = vertex.addEdge(newLocation, "HasPosition");
                    edge.save();
                }

                if (item.getProgressInfo() != null) {

                    if (edge == null) {
                        edge = vertex.getEdges(ODirection.OUT, "HasPosition").iterator().next();
                    }
                    edge.setProperty("progress", item.getProgressInfo().getProgress());
                    edge.setProperty("datetime", item.getProgressInfo().getDatetime());
                    edge.save();
                }
            }
            return vertexToItem(vertex);

        } catch (Exception e) {
            throw new RuntimeException("Error while updating item with ID " + item.getId() + ": " + e.getMessage(), e);
        }
    }

    public void deleteItem(String id) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            if (vertex != null) {
                vertex.delete();
            } else {
                throw new IllegalArgumentException("Item with ID: " + id + " not found for deletion.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting item with ID " + id + ": " + e.getMessage(), e);
        }
    }
    

    private Item vertexToItem(OVertex vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Attempted to convert a null vertex to item.");
        }
        return new Item(
            vertex.getIdentity().toString(),
            vertex.getProperty("name"),
            vertex.getProperty("speed"),
            vertex.getProperty("active"),
            vertex.getProperty("properties")
        );
    }
    
}
