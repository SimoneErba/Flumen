package com.livedatatrail.backend.services;

import com.livedatatrail.backend.models.Location;
import com.livedatatrail.backend.utils.OrientDBUtils;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {

    private final OrientDBService orientDBService;

    @Autowired
    public LocationService(OrientDBService orientDBService) {
        this.orientDBService = orientDBService;
    }

    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        try (ODatabaseSession db = orientDBService.getSession()) {
            try (OResultSet rs = db.query("SELECT * FROM Location")) {
                while (rs.hasNext()) {
                    OResult row = rs.next();
                    row.getVertex().ifPresent(vertex -> {
                        Location location = vertexToLocation(vertex);
                        locations.add(location);
                    });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching locations: " + e.getMessage(), e);
        }

        return locations;
    }

    public Location getLocationById(String id) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            Location location = vertexToLocation(vertex);
            return location;
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching location with ID " + id + ": " + e.getMessage(), e);
        }
    }

    public Location createLocation(String name, double latitude, double longitude) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            if (OrientDBUtils.checkIfAlreadyExists(db, name)) {
                throw new IllegalArgumentException("Location with name " + name + " already exists.");
            }
            OVertex vertex = db.newVertex("Location");
            vertex.setProperty("name", name);
            vertex.setProperty("latitude", latitude);
            vertex.setProperty("longitude", longitude);
            vertex.save();
            var location = vertexToLocation(vertex);
            return location;
        } catch (Exception e) {
            throw new RuntimeException("Error while creating location with name " + name + ": " + e.getMessage(), e);
        }
    }

    public Location updateLocation(String id, String name, double latitude, double longitude) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            if (vertex != null) {
                vertex.setProperty("name", name);
                vertex.setProperty("latitude", latitude);
                vertex.setProperty("longitude", longitude);
                vertex.save();
                return vertexToLocation(vertex);
            } else {
                throw new IllegalArgumentException("Location with ID: " + id + " not found.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while updating location with ID " + id + ": " + e.getMessage(), e);
        }
    }

    public void deleteLocation(String id) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            ORID theRid = new ORecordId(id);
            OVertex vertex = db.load(theRid);
            if (vertex != null) {
                vertex.delete();
            } else {
                throw new IllegalArgumentException("Location with ID: " + id + " not found for deletion.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting location with ID " + id + ": " + e.getMessage(), e);
        }
    }

    private Location vertexToLocation(OVertex vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Attempted to convert a null vertex to location.");
        }
        return new Location(vertex.getIdentity().toString(), vertex.getProperty("name"), vertex.getProperty("latitude"),
                vertex.getProperty("longitude"));
    }
}
