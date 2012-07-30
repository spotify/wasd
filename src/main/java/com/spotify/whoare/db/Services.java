package com.spotify.whoare.db;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.TextParseException;

import java.util.*;

@Slf4j
@Data
public class Services {
    private final Map<String, Service> nameServiceMap;
    private final List<Service> services;

    private final Config config;

    Services(Config config) {
        this.config = config;
        nameServiceMap = new HashMap<String, Service>();
        services = new LinkedList<Service>();
    }

    protected void fill(Database database) throws TextParseException {
        final Set<Map.Entry<String,ConfigValue>> entries = config.root().entrySet();
        for (Map.Entry<String, ConfigValue> entry: entries) {
            final String name = entry.getKey();
            log.debug("Grabbing service {}", name);

            final ConfigValue configValue = entry.getValue();

            final Service service = new Service(entry.getKey(), configValue, database);
            services.add(service);
            nameServiceMap.put(name, service);
        }
    }
}
