package com.livedatatrail.backend.services;

import com.livedatatrail.backend.models.Location;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class LocationService {

    private final OrientDBService orientDBService;
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    @Autowired
    public LocationService(OrientDBService orientDBService) {
        this.orientDBService = orientDBService;
    }

    public List<Location> getAllLocations() {
        logger.info("Fetching all locations...");
        List<Location> locations = new ArrayList<>();
        try (ODatabaseSession db = orientDBService.getSession()) {
            try (OResultSet rs = db.query("SELECT * FROM Location")) {
                while (rs.hasNext()) {
                    OResult row = rs.next();
                    row.getVertex().ifPresent(vertex -> {
                        Location location = vertexToLocation(vertex);
                        locations.add(location);
                        logger.debug("Location found: {}", location);
                    });
                }
            }
        } catch (Exception e) {
            logger.error("Error while getting location: {}", e.getMessage());
        }
        
        logger.info("Total locations fetched: {}", locations.size());
        return locations;
    }

    public Location getLocationById(String id) {
        logger.info("Fetching location with ID: {}", id);
        
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            Location location = vertexToLocation(vertex);
            logger.debug("Location fetched: {}", location);
            return location;
        } catch (Exception e) {
            logger.error("Error while getting location: {}", e.getMessage());
        }
        
        return null;
    }

    public Location createLocation(String name, double latitude, double longitude) {
        logger.info("Creating new location with name: {}, latitude: {}, longitude: {}", name, latitude, longitude);
    
        try (ODatabaseSession db = orientDBService.getSession()) {
            OVertex vertex = db.newVertex("Location");
            vertex.setProperty("name", name);
            vertex.setProperty("latitude", latitude);
            vertex.setProperty("longitude", longitude);
            vertex.save();
            var location = vertexToLocation(vertex);
            logger.info("New location created: {}", location);
            return location;
        } catch (Exception e) {
            logger.error("Error while creating location: {}", e.getMessage());
        }
        
        return null;
    }
    
    public Location updateLocation(String id, String name, double latitude, double longitude) {
        logger.info("Updating location with ID: {}", id);
        
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            if (vertex != null) {
                vertex.setProperty("name", name);
                vertex.setProperty("latitude", latitude);
                vertex.setProperty("longitude", longitude);
                vertex.save();
                Location updatedLocation = vertexToLocation(vertex);
                logger.info("Location updated: {}", updatedLocation);
                return updatedLocation;
            } else {
                logger.warn("Location with ID: {} not found", id);
            }
        } catch (Exception e) {
            logger.error("Error while updating location: {}", e.getMessage());
        }
        
        return null;
    }

    public void deleteLocation(String id) {
        logger.info("Deleting location with ID: {}", id);
        orientDBService.withSession(db -> {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            if (vertex != null) {
                vertex.delete();
                logger.info("Location with ID: {} deleted", id);
            } else {
                logger.warn("Location with ID: {} not found for deletion", id);
            }
        });
    }

    private Location vertexToLocation(OVertex vertex) {
        if (vertex == null) {
            logger.warn("Attempted to convert a null vertex to location");
            return null;
        }
        return new Location(
            vertex.getIdentity().toString(),
            vertex.getProperty("name"),
            vertex.getProperty("latitude"),
            vertex.getProperty("longitude")
        );
    }
}

