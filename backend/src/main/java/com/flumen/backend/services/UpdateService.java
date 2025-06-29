package com.flumen.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flumen.backend.models.Location;
import com.flumen.backend.models.UpdateModel;
import com.flumen.backend.utils.OrientDBUtils;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;

@Service
public class UpdateService {
	private static final Logger logger = LoggerFactory.getLogger(PositionService.class);

	private final OrientDBService orientDBService;

	@Autowired
	public UpdateService(OrientDBService orientDBService) {
		this.orientDBService = orientDBService;
	}

	public OVertex updateVertex(UpdateModel model) {
		try (ODatabaseSession db = orientDBService.getSession()) {
			var location = OrientDBUtils.loadAndValidateVertexByCustomId(db, model.getId());
			if (location != null) {
				model.getProperties().forEach((key, value) -> {
					location.setProperty(key, value);
				});
				location.save();
				return location;
			} else {
				throw new IllegalArgumentException("vertex with ID: " + model.getId() + " not found.");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while updating vertex with ID " + model.getId() + ": " + e.getMessage(),
					e);
		}
	}
}
