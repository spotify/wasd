package com.spotify.whoare;

import com.spotify.whoare.db.Database;
import com.spotify.whoare.db.Service;
import com.typesafe.config.ConfigFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class DbTest {
    public static void main(String[] args) throws IOException, JsonGenerationException {
        Database.rebuild(ConfigFactory.load());
        ObjectMapper mapper = new ObjectMapper();

        final Database current = Database.current();

        for (Service service: current.getServices().getServices()) {
            System.out.printf("%s: %s\n", service.getName(), service.getHosts().toString());
        }
    }
}
