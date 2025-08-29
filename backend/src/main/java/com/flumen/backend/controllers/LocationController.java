package com.flumen.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.flumen.backend.domain.Location;
import com.flumen.backend.models.UpdateModel;
import com.flumen.backend.models.input.LocationInput;
import com.flumen.backend.services.LocationService;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<Location> getAllLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/{id}")
    public Location getLocationById(@PathVariable String id) {
        return locationService.getLocationById(id);
    }

    @PostMapping
    public Location createLocation(@RequestBody LocationInput location) {
        return locationService.createLocation(location);
    }

    @PutMapping()
    public Location updateLocation(@RequestBody UpdateModel model) {
        return locationService.updateLocation(model);
    }

    @DeleteMapping("/{id}")
    public void deleteLocation(@PathVariable String id) {
        locationService.deleteLocation(id);
    }
}
