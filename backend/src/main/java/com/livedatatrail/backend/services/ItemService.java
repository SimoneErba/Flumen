package com.livedatatrail.backend.services;

import com.livedatatrail.backend.models.Item;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

    private final OrientDBService orientDBService;
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    @Autowired
    public ItemService(OrientDBService orientDBService) {
        this.orientDBService = orientDBService;
    }

    public List<Item> getAllItems() {
        logger.info("Fetching all items...");
        List<Item> items = new ArrayList<>();
        try (ODatabaseSession db = orientDBService.getSession()) {
            try (var rs = db.query("SELECT * FROM Item")) {
                while (rs.hasNext()) {
                    var row = rs.next();
                    row.getVertex().ifPresent(vertex -> {
                        Item item = vertexToItem(vertex);
                        items.add(item);
                        logger.debug("Item found: {}", item);
                    });
                }
            }
        } catch (Exception e) {
            logger.error("Error while fetching items: {}", e.getMessage());
        }
        return items;
    }

    public Item getItemById(String id) {
        logger.info("Fetching item with ID: {}", id);
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            return vertex != null ? vertexToItem(vertex) : null;
        } catch (Exception e) {
            logger.error("Error while fetching item: {}", e.getMessage());
            return null;
        }
    }

    public Item createItem(String name, Map<String, Object> properties) {
        logger.info("Creating new item with name: {}", name);
        try (ODatabaseSession db = orientDBService.getSession()) {
            OVertex vertex = db.newVertex("Item");
            vertex.setProperty("name", name);
            vertex.setProperty("properties", properties);
            vertex.save();
            return vertexToItem(vertex);
        } catch (Exception e) {
            logger.error("Error while creating item: {}", e.getMessage());
            return null;
        }
    }

    public Item updateItem(String id, String name, Map<String, Object> properties) {
        logger.info("Updating item with ID: {}", id);
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            if (vertex != null) {
                vertex.setProperty("name", name);
                vertex.setProperty("properties", properties);
                vertex.save();
                return vertexToItem(vertex);
            } else {
                logger.warn("Item with ID: {} not found", id);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while updating item: {}", e.getMessage());
            return null;
        }
    }

    public void deleteItem(String id) {
        logger.info("Deleting item with ID: {}", id);
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            if (vertex != null) {
                vertex.delete();
                logger.info("Item with ID: {} deleted", id);
            } else {
                logger.warn("Item with ID: {} not found", id);
            }
        } catch (Exception e) {
            logger.error("Error while deleting item: {}", e.getMessage());
        }
    }

    private Item vertexToItem(OVertex vertex) {
        return new Item(
            vertex.getIdentity().toString(),
            vertex.getProperty("name"),
            vertex.getProperty("properties")
        );
    }
}
