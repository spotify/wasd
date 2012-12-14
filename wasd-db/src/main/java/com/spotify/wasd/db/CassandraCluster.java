package com.spotify.wasd.db;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@ToString(of = "record", includeFieldNames = false)
public class CassandraCluster {
    @Getter
    private final Record record;
    @Getter
    private final Set<Service> serviceSet;
    @Getter
    private final Map<Site, CassandraSiteCluster> siteCassandraSiteClusterMap;
    @Getter
    private final Set<CassandraSiteCluster> cassandraSiteClusterSet;
    @Getter(lazy = true)
    private final Set<Host> hostSet = grabHostSet();

    CassandraCluster(Hosts hosts, Record record) {
        this.record = record;
        serviceSet = new HashSet<Service>();
        siteCassandraSiteClusterMap = new HashMap<Site, CassandraSiteCluster>();
        cassandraSiteClusterSet = new HashSet<CassandraSiteCluster>();

        for (Map.Entry<Site, SiteRecord> entry : record.getSiteSiteRecordMap().entrySet()) {
            final CassandraSiteCluster siteCluster = new CassandraSiteCluster(hosts, entry.getValue(), this);
            siteCassandraSiteClusterMap.put(entry.getKey(), siteCluster);
            cassandraSiteClusterSet.add(siteCluster);
        }
    }

    void addToService(Service service) {
        serviceSet.add(service);
    }

    private Set<Host> grabHostSet() {
        final Set<Host> hosts = new HashSet<Host>();

        for (CassandraSiteCluster siteCluster : cassandraSiteClusterSet) {
            hosts.addAll(siteCluster.getHostSet());
        }

        return hosts;
    }
}
