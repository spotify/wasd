package com.spotify.wasd.db;

import com.typesafe.config.Config;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DatabaseHolder {
    private Database current;

    public void rebuild(Config config) throws IOException {
        try {
            current = new Database(config);
        } catch (ExecutionException e) {
            throw new IOException(e); /* somewhat ugly, but we don't want to break the best-known public API */
        }
    }

    public Database current() {
        return current;
    }
}
