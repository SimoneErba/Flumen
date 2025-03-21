#!/bin/bash

# Start OrientDB in the background
/orientdb/bin/server.sh &

# Wait for OrientDB to be ready
echo "[entrypoint] Waiting for OrientDB to start..."
for i in {1..30}; do
    if curl -s http://0.0.0.0:2480 > /dev/null; then
        echo "[entrypoint] OrientDB is ready!"
        break
    fi
    echo "[entrypoint] Waiting... attempt $i/30"
    sleep 2
done

# Create the database
echo "[entrypoint] Creating main database..."
/orientdb/bin/console.sh "CREATE DATABASE remote:0.0.0.0/main root rootpwd plocal graph" || {
    echo "[entrypoint] Failed to create database"
}

echo "[entrypoint] Setting up permissions..."
/orientdb/bin/console.sh "CONNECT remote:0.0.0.0/main root rootpwd; GRANT ALL ON database.* TO root"

# Keep container running by waiting for the OrientDB process
wait