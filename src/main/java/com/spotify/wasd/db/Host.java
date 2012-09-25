package com.spotify.wasd.db;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@ToString(of = {"address"}, includeFieldNames = false)
public class Host {
    @Getter
    private final InetAddress address;
    @Getter
    private final String reverseName;
    @Getter
    private final Set<SiteRecord> siteRecordSet = new HashSet<SiteRecord>();
    @Getter
    private final Set<CassandraSiteCluster> cassandraSiteClusterSet = new HashSet<CassandraSiteCluster>();

    void addToSiteRecord(SiteRecord siteRecord) {
        siteRecordSet.add(siteRecord);
    }

    void addToCassandraSiteCluster(CassandraSiteCluster cassandraSiteCluster) {
        cassandraSiteClusterSet.add(cassandraSiteCluster);
    }

    @Getter(lazy = true)
    private final Set<Service> serviceSet = grabServiceSet();

    public Set<Service> grabServiceSet() {
        Set<Service> services = new HashSet<Service>();

        for (SiteRecord siteRecord : siteRecordSet)
            services.addAll(siteRecord.getRecord().getServiceSet());

        for (CassandraSiteCluster cassandraSiteCluster: cassandraSiteClusterSet)
            services.addAll(cassandraSiteCluster.getCassandraCluster().getServiceSet());

        return services;
    }

    @Getter(lazy = true)
    private final Map<Service, Set<Site>> serviceSiteMap = computeServiceSiteMap();

    private Map<Service, Set<Site>> computeServiceSiteMap() {
        Map<Service, Set<Site>> serviceSiteMap = new HashMap<Service, Set<Site>>();

        for (SiteRecord siteRecord : siteRecordSet) {
            final Site site = siteRecord.getSite();
            for (Service service : siteRecord.getRecord().getServiceSet()) {
                if (!serviceSiteMap.containsKey(service))
                    serviceSiteMap.put(service, new HashSet<Site>());
                serviceSiteMap.get(service).add(site);
            }
        }

        for (CassandraSiteCluster siteCluster : cassandraSiteClusterSet) {
            final Site site = siteCluster.getSiteRecord().getSite();
            for (Service service : siteCluster.getCassandraCluster().getServiceSet()) {
                if (!serviceSiteMap.containsKey(service))
                    serviceSiteMap.put(service, new HashSet<Site>());
                serviceSiteMap.get(service).add(site);
            }
        }

        return serviceSiteMap;
    }
}
