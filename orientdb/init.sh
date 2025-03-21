#!/bin/bash

echo "[init] Starting OrientDB initialization script..."

# Function to check if OrientDB is ready
check_orientdb() {
    echo "[init] Checking if OrientDB is ready..."
    for i in {1..30}; do
        if curl -s http://localhost:2480 > /dev/null; then
            echo "[init] OrientDB is ready!"
            return 0
        fi
        echo "[init] Waiting for OrientDB... attempt $i/30"
        sleep 2
    done
    echo "[init] OrientDB did not become ready in time"
    return 1
}

# Wait for OrientDB to be ready
check_orientdb || exit 1

echo "[init] Creating main database..."
# Create the database using console
/orientdb/bin/console.sh "CREATE DATABASE remote:localhost/main root rootpwd plocal graph" || {
    echo "[init] Failed to create database"
    exit 1
}

echo "[init] Setting up permissions..."
# Grant necessary permissions
/orientdb/bin/console.sh "CONNECT remote:localhost/main root rootpwd; GRANT ALL ON database.* TO root" || {
    echo "[init] Failed to set permissions"
    exit 1
}

echo "[init] Database initialization completed successfully!" 