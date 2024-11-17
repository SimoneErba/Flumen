package com.livedatatrail.backend.services;

import com.livedatatrail.backend.models.Item;
import com.livedatatrail.backend.models.input.ItemInput;
import com.livedatatrail.backend.utils.OrientDBUtils;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OVertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

    private final OrientDBService orientDBService;

    @Autowired
    public ItemService(OrientDBService orientDBService) {
        this.orientDBService = orientDBService;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        try (ODatabaseSession db = orientDBService.getSession()) {
            try (var rs = db.query("SELECT * FROM Item")) {
                while (rs.hasNext()) {
                    var row = rs.next();
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
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            return vertex != null ? vertexToItem(vertex) : null;
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching item with ID " + id + ": " + e.getMessage(), e);
        }
    }

    public Item createItem(ItemInput item) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            if (OrientDBUtils.checkIfAlreadyExists(db, item.getId())) {
                throw new IllegalArgumentException("Item with id " + item.getId() + " already exists.");
            }
            OVertex vertex = db.newVertex("Item");
            vertex.setProperty("customId", item.getId());
            vertex.setProperty("name", item.getName());
            vertex.setProperty("properties", item.getProperties());
            vertex.save();
            return vertexToItem(vertex);
        } catch (Exception e) {
            throw new RuntimeException("Error while creating item with name " + item.getName() + ": " + e.getMessage(),
                    e);
        }
    }

    public Item updateItem(String id, String name, Map<String, Object> properties) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            if (vertex != null) {
                vertex.setProperty("name", name);
                vertex.setProperty("properties", properties);
                vertex.save();
                return vertexToItem(vertex);
            } else {
                throw new IllegalArgumentException("Item with ID: " + id + " not found.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while updating item with ID " + id + ": " + e.getMessage(), e);
        }
    }

    public void deleteItem(String id) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            if (vertex != null) {
                vertex.delete();
            } else {
                throw new IllegalArgumentException("Item with ID: " + id + " not found.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting item with ID " + id + ": " + e.getMessage(), e);
        }
    }

    private Item vertexToItem(OVertex vertex) {
        return new Item(vertex.getProperty("customId").toString(), vertex.getProperty("name"),
                vertex.getProperty("properties"));
    }
}
