package com.spotify.wasd.db;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.Resolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@ToString(of = "id", includeFieldNames = false)
public class Record {
    protected enum Type {
        SRV,
        CNAME,
        A,
        PTR,
    }

    @Getter
    private final RecordIdentifier id;
    @Getter
    private final Map<Site, SiteRecord> siteSiteRecordMap;
    @Getter
    private final Set<Service> serviceSet = new HashSet<Service>();

    Record(RecordIdentifier id, Sites sites, Hosts hosts, Resolver resolver) throws IOException {
        this.id = id;
        siteSiteRecordMap = new HashMap<Site, SiteRecord>();

        for (Site site : sites.getSiteSet()) {
            Record.log.debug("Getting {} in {}", id, site);
            siteSiteRecordMap.put(site, new SiteRecord(this, site, hosts, resolver));
        }
    }

    @Getter(lazy = true)
    private final Set<Host> hostSet = grabHostSet();

    private Set<Host> grabHostSet() {
        Set<Host> hosts = new HashSet<Host>();
        for (Map.Entry<Site, SiteRecord> entry : siteSiteRecordMap.entrySet())
            hosts.addAll(entry.getValue().getHostSet());

        return hosts;
    }

    void addToService(Service service) {
        serviceSet.add(service);
    }
}
