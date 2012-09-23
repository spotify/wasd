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

    private static Database current = null;

    Database(Config config) throws IOException {
        sites = new Sites(config.getConfig("Sites"));
        hosts = new Hosts();
        records = new Records(sites, hosts);
        services = new Services(config.getConfig("Services"), records, hosts);
    }
}
