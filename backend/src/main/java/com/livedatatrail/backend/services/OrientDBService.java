package com.livedatatrail.backend.services;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
                session.createVertexClass("Location");
                logger.info("Created vertex class: Location");
            }
            if (session.getClass("Item") == null) {
                session.createVertexClass("Item");
                logger.info("Created vertex class: Item");
            }
            if (session.getClass("HasPosition") == null) {
                session.createEdgeClass("HasPosition");
                logger.info("Created edge class: HasPosition");
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
