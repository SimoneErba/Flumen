package com.livedatatrail.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		var orientDB = new OrientDB("remote:localhost", "root", "rootpwd", OrientDBConfig.defaultConfig());
        // orientDB.createIfNotExists("main", ODatabaseType.MEMORY);
        var db = orientDB.open("main", "root", "rootpwd");
		// db.getMetadata().getSchema().dropClass("Location");
		OClass location = db.getClass("Location");
		if (location == null){
			db.createVertexClass("Location");
		}
		OClass item = db.getClass("Item");
		if (item == null){
			db.createVertexClass("Item");
		}
		db.close();
		orientDB.close();
		SpringApplication.run(BackendApplication.class, args);
	}

}
