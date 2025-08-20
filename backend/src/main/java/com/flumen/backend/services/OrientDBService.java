package com.flumen.backend.services;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.flumen.backend.entities.Location;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class OrientDBService {
    private static final Logger logger = LoggerFactory.getLogger(OrientDBService.class);

    private OrientDB orientDB;
    private ODatabasePool pool;

    @Value("${orientdb.url}")
    private String dbUrl;

    @Value("${orientdb.username}")
    private String username;

    @Value("${orientdb.password}")
    private String password;

    @PostConstruct
    public void init() {
        orientDB = new OrientDB(dbUrl, username, password, OrientDBConfig.defaultConfig());

        OrientDBConfig poolConfig = OrientDBConfig.builder()
                .addConfig(OGlobalConfiguration.DB_POOL_MIN, 5)
                .addConfig(OGlobalConfiguration.DB_POOL_MAX, 10)
                .build();

        pool = new ODatabasePool(orientDB, "main", username, password, poolConfig);
        logger.info("OrientDB connection pool initialized.");

        withSession(session -> {
            if (session.getClass("Location") == null) {
                OClass locationClass = session.createVertexClass("Location");
                locationClass.createProperty("customId", OType.STRING);
                logger.info("Created vertex class: Location");
            }
            if (session.getClass("Item") == null) {
                OClass itemClass = session.createVertexClass("Item");
                itemClass.createProperty("customId", OType.STRING);
                logger.info("Created vertex class: Item");
            }
            if (session.getClass("GeoLocation") == null) {
                session.createVertexClass("GeoLocation").setSuperClasses(Arrays.asList(session.getClass("Location")));
                logger.info("Created vertex class: GeoLocation");
            }
            if (session.getClass("MovingItem") == null) {
                session.createVertexClass("MovingItem").setSuperClasses(Arrays.asList(session.getClass("Item")));
                logger.info("Created vertex class: MovingItem");
            }
            if (session.getClass("Road") == null) {
                session.createVertexClass("Road").setSuperClasses(Arrays.asList(session.getClass("Location")));
                logger.info("Created vertex class: Road");
            }
            if (session.getClass("Conveyor") == null) {
                session.createVertexClass("Conveyor").setSuperClasses(Arrays.asList(session.getClass("Road")));
                logger.info("Created vertex class: Conveyor");
            }
            if (session.getClass("HasPosition") == null) {
                session.createEdgeClass("HasPosition");
                logger.info("Created edge class: HasPosition");
            }

            if (session.getClass("ConnectedTo") == null) {
                session.createEdgeClass("ConnectedTo");
                logger.info("Created edge class: ConnectedTo");
            }

            OClass itemClass = session.getClass("Item");
            if (itemClass != null && itemClass.getClassIndex("Item.id_unique") == null) {
                itemClass.createIndex("Item.id_unique", OClass.INDEX_TYPE.UNIQUE, "customId");
                logger.info("Created unique index on Item.id");
            }

            OClass locationClass = session.getClass("Location");
            if (locationClass != null && locationClass.getClassIndex("Location.id_unique") == null) {
                locationClass.createIndex("Location.id_unique", OClass.INDEX_TYPE.UNIQUE, "customId");
                logger.info("Created unique index on Location.id");
            }
        });
    }

    @PreDestroy
    public void close() {
        if (pool != null) {
            pool.close();
            logger.info("OrientDB connection pool closed.");
        }
        if (orientDB != null) {
            orientDB.close();
            logger.info("OrientDB instance closed.");
        }
    }

    public void withSession(SessionCallback callback) {
        try (var session = pool.acquire()) {
            callback.execute(session);
        } catch (Exception e) {
            logger.error("Error executing session callback", e);
        }
    }

    public interface SessionCallback {
        void execute(ODatabaseSession session);
    }

    public ODatabaseSession getSession() {
        return pool.acquire();
    }
}
