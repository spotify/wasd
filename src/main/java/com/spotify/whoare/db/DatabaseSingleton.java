package com.spotify.whoare.db;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;

public class DatabaseSingleton {
    private static Database current;

    public static void rebuild(Config config) throws IOException {
        current = new Database(config);
    }

    public static Database current() throws IOException {
        if (current == null)
            rebuild(ConfigFactory.load());

        return current;
    }
}
