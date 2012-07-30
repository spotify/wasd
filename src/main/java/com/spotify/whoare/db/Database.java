package com.spotify.whoare.db;

import com.typesafe.config.Config;
import lombok.Data;
import org.xbill.DNS.TextParseException;

import java.net.UnknownHostException;

@Data
public class Database {
    private final Sites sites;
    private final Hosts hosts;
    private final Services services;
    private Records records;

    private static Database current;

    public static void rebuild(Config config) throws TextParseException, UnknownHostException {
        Config sitesConfig = config.getConfig("Sites");
        final Sites sites = new Sites(sitesConfig);
        final Hosts hosts = new Hosts();
        final Services services = new Services(config.getConfig("Services"));
        final Database inConstruction = new Database(sites, hosts, services);
        inConstruction.setRecords(new Records(inConstruction));
        services.fill(inConstruction);

        current = inConstruction;
    }

    public static Database current() {
        return current;
    }
}
