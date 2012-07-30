package com.spotify.whoare.db;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;

import java.util.*;

@Slf4j
@Data
public class Record {
    protected enum Type {
        SRV,
        CNAME,
        A
    }

    private final RecordIdentifier id;
    private final Map<Site, SiteRecord> zoneRecords;

    protected Record(RecordIdentifier id, Database database, Resolver resolver) throws TextParseException {
        this.id = id;
        zoneRecords = new HashMap<Site, SiteRecord>();

        for (Site site: database.getSites().getSiteList()) {
            log.debug("Getting {} in {}", id, site);
            zoneRecords.put(site, new SiteRecord(id, site, database, resolver));
        }
    }

    @Getter(lazy=true) private final Set<Host> hosts = getHostSet();

    private Set<Host> getHostSet() {
        Set<Host> hosts = new HashSet<Host>();
        for (Map.Entry<Site, SiteRecord> entry: zoneRecords.entrySet())
            hosts.addAll(entry.getValue().getHostSet());

        return hosts;
    }
}
