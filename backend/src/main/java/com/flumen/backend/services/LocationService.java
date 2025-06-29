package com.flumen.backend.services;

import com.flumen.backend.domain.Location;
import flumen.events.DomainEvent;
import com.flumen.backend.models.UpdateModel;
import com.flumen.backend.models.input.LocationInput;
import com.flumen.backend.utils.OrientDBUtils;
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
    private final UpdateService updateService;

    @Autowired
    public LocationService(OrientDBService orientDBService, UpdateService updateService) {
        this.orientDBService = orientDBService;
        this.updateService = updateService;
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
            var vertex = OrientDBUtils.loadAndValidateVertexByCustomId(db, id);
            Location location = vertexToLocation(vertex);
            return location;
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching location with ID " + id + ": " + e.getMessage(), e);
        }
    }

    public Location createLocation(LocationInput location) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            if (OrientDBUtils.checkIfAlreadyExists(db, location.getId())) {
                throw new IllegalArgumentException("Location with name " + location.getName() + " already exists.");
            }
            OVertex vertex = db.newVertex("Location");
            vertex.setProperty("name", location.getName());
            vertex.setProperty("customId", location.getId());
            vertex.setProperty("latitude", location.getLatitude());
            vertex.setProperty("longitude", location.getLongitude());
            vertex.setProperty("length", location.getLength());
            vertex.setProperty("speed", location.getSpeed());
            vertex.setProperty("type", location.getType());
            vertex.setProperty("active", location.getActive());
            vertex.save();
            return vertexToLocation(vertex);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while creating location with name " + location.getName() + ": " + e.getMessage(), e);
        }
    }

    public Location updateLocation(UpdateModel model) {
        return vertexToLocation(this.updateService.updateVertex(model));
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
        return new Location(
            vertex.getIdentity().toString(),
            vertex.getProperty("name"),
            vertex.getProperty("latitude"),
            vertex.getProperty("longitude"),
            vertex.getProperty("length"),
            vertex.getProperty("speed"),
            vertex.getProperty("type"),
            vertex.getProperty("active"),
            vertex.getProperty("properties")
        );
    }
}
