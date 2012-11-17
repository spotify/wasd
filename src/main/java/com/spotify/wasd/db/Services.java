package com.spotify.wasd.db;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@RequiredArgsConstructor
public class Services {
    @Getter
    private final Map<String, Service> nameServiceMap;
    @Getter
    private final Set<Service> serviceSet;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    Services(final Config config, final Records records, final Hosts hosts) throws ExecutionException {
        nameServiceMap = new HashMap<String, Service>();
        serviceSet = new HashSet<Service>();

        final Set<Map.Entry<String, ConfigValue>> entries = config.root().entrySet();
        final int total = entries.size();

        final List<Callable<Exception>> tasks = new LinkedList<Callable<Exception>>();

        int counter = 1;
        for (Map.Entry<String, ConfigValue> entry : entries) {
            final String name = entry.getKey();
            final String descr = String.format("%s [%d/%d]", name, counter, total);
            final ConfigValue configValue = entry.getValue();

            tasks.add(new Callable<Exception>() {
                @Override
                public Exception call() throws /* passed-on */ Exception {
                    try {
                        grabService(records, hosts, name, descr, configValue);
                    } catch (IOException e) {
                        return e;
                    }
                    return null;
                }
            });

            counter += 1;
        }

        final List<Future<Exception>> futures;
        try {
            futures = executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            log.error("Execution interrupted!", e);
            throw new ExecutionException(e);
        }

        for (Future<Exception> future: futures) {
            Exception exception = null;
            try {
                exception = future.get();
                if (exception != null)
                    throw new ExecutionException(exception);
            } catch (InterruptedException e) {
                throw new ExecutionException(e);
            }
        }
    }

    private void grabService(Records records, Hosts hosts, String name, String descr, ConfigValue configValue) throws IOException {
        Services.log.info("Grabbing service {}", descr);
        final Service service = new Service(name, configValue, records, hosts);
        serviceSet.add(service);
        nameServiceMap.put(name, service);
        Services.log.info("Grabbed service {}", descr);
    }
}
