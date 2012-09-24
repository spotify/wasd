package com.spotify.whoare.db;

import com.typesafe.config.Config;

import java.io.IOException;

public class DatabaseHolder {
    private Database current;

    public void rebuild(Config config) throws IOException {
        current = new Database(config);
    }

    public Database current() {
        return current;
    }
}
