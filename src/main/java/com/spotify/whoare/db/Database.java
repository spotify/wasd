package com.spotify.whoare.db;

import com.typesafe.config.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
public class Database {
    @Getter
    private final Sites sites;
    @Getter
    private final Hosts hosts;
    @Getter
    private final Services services;
    @Getter
    private final Records records;

    private static Database current;

    public static void rebuild(Config config) throws IOException {
        final Sites sites = new Sites(config.getConfig("Sites"));
        final Hosts hosts = new Hosts();
        final Records records = new Records(sites, hosts);
        final Services services = new Services(config.getConfig("Services"), records, hosts);
        current = new Database(sites, hosts, services, records);
    }

    public static Database current() {
        return current;
    }
}
