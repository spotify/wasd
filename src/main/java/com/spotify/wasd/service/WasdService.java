package com.spotify.wasd.service;

import com.spotify.wasd.db.DatabaseHolder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WasdService extends Service<Configuration> {

    private final DatabaseHolder holder = new DatabaseHolder();

    @Override
    protected void initialize(Configuration configuration, Environment environment) throws Exception {
        try {
            final Thread firstUpdateThread = new UpdateThread(ConfigFactory.load(), true, holder);
            log.info("Starting first DB update");
            firstUpdateThread.start();
            firstUpdateThread.join();
            log.info("First DB update finished");
        } catch (Exception e) {
            log.error("Could not perform first update: {}", e);
            System.exit(43);
        }

        new ContinuousUpdateThread().start();

        environment.addResource(new ServiceResource(holder));
        environment.addResource(new HostResource(holder));
        environment.addResource(new FindHostResource(holder));
    }

    WasdService() {
        super("wasd");
    }

    public static void main(String[] args) throws Exception {
        new WasdService().run(args);
    }

    @Slf4j
    @RequiredArgsConstructor
    private static class UpdateThread extends Thread {
        private final Config config;
        private final boolean exitOnFail;
        private final DatabaseHolder holder;

        private final Timer updates = Metrics.newTimer(UpdateThread.class, "updates", TimeUnit.SECONDS, TimeUnit.HOURS);

        @Override
        public void run() {
            this.setName("wasd-update-" + System.currentTimeMillis());

            TimerContext ctx = updates.time();
            try {
                UpdateThread.log.info("Running update thread");
                holder.rebuild(config);
            } catch (IOException e) {
                UpdateThread.log.error("Couldn't rebuild database ({})!", e);
                if (exitOnFail)
                    System.exit(44);
            } finally {
                ctx.stop();
            }
        }
    }

    private class ContinuousUpdateThread extends Thread {
        @Override
        public void run() {
            this.setName("wasd-continuous-update");

            while (true) {
                try {
                    /* Reload config continuously */
                    final Config config = ConfigFactory.load();

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
