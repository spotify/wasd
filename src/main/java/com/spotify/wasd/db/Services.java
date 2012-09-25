package com.spotify.wasd.db;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class Services {
    @Getter
    private final Map<String, Service> nameServiceMap;
    @Getter
    private final Set<Service> serviceSet;

    Services(Config config, Records records, Hosts hosts) throws IOException {
        nameServiceMap = new HashMap<String, Service>();
        serviceSet = new HashSet<Service>();

        final Set<Map.Entry<String, ConfigValue>> entries = config.root().entrySet();
        for (Map.Entry<String, ConfigValue> entry : entries) {
            final String name = entry.getKey();
            final ConfigValue configValue = entry.getValue();

            Services.log.info("Grabbing service {}", name);
            final Service service = new Service(entry.getKey(), configValue, records, hosts);
            Services.log.info("Grabbed service {}", name);

            serviceSet.add(service);
            nameServiceMap.put(name, service);
        }
    }
}
