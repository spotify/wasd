package com.spotify.whoare.service;

import com.spotify.whoare.db.DatabaseHolder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class WhoareService extends Service<Configuration> {

    private final DatabaseHolder holder = new DatabaseHolder();

    @Override
    protected void initialize(Configuration configuration, Environment environment) throws Exception {
        try {
            final Thread firstUpdateThread = new UpdateThread(ConfigFactory.load(), true, holder);
            firstUpdateThread.start();
            firstUpdateThread.join();
        } catch (Exception e) {
            log.error("Could not perform first update: {}", e);
            System.exit(1);
        }

        new ContinuousUpdateThread().start();

        environment.addResource(new ServiceResource(holder));
        environment.addResource(new HostResource(holder));
    }

    WhoareService() {
        super("whoare");
    }

    public static void main(String[] args) throws Exception {
        new WhoareService().run(args);
    }

    @Slf4j
    @RequiredArgsConstructor
    private static class UpdateThread extends Thread {
        private final Config config;
        private final boolean exitOnFail;
        private final DatabaseHolder holder;

        @Override
        public void run() {
            try {
                UpdateThread.log.info("Running update thread");
                holder.rebuild(config);
            } catch (IOException e) {
                UpdateThread.log.error("Couldn't rebuild database ({})!", e);
                if (exitOnFail)
                    System.exit(1);
            }
        }
    }

    private class ContinuousUpdateThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    /* Reload config continuously */
                    final Config config = ConfigFactory.load();
                    this.setName(String.format("whoare-update-%d", System.currentTimeMillis()));

                    final Thread updateThread = new UpdateThread(config, false, holder);
                    updateThread.start();
                    Thread.sleep(config.getMilliseconds("Server.RefreshRate"));
                    updateThread.join(); /* avoid parallelism if things are getting really slow */
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
