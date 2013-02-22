package com.spotify.wasd.db;

import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
@ToString(of = "name", includeFieldNames = false)
public class Service {
    @Getter
    private final String name;
    @Getter
    private final Set<Record> recordSet;
    @Getter
    private final Set<CassandraCluster> cassandraClusterSet;
    @Getter
    private final Map<String, HashSet<Contact>> contactMap;

    Service(String name, ConfigValue configValue, Records records, Hosts hosts) throws IOException {
        this.name = name;
        recordSet = new HashSet<Record>();
        cassandraClusterSet = new HashSet<CassandraCluster>();
        contactMap = new HashMap<String, HashSet<Contact>>();

        final Map<String, ConfigObject> configDict =
                (Map<String, ConfigObject>) configValue.unwrapped();

        final List<Map<String, String>> recordConfigsList =
                (List<Map<String, String>>) configDict.get("Records");
        if (recordConfigsList != null)
            addRecordConfigs(records, recordConfigsList);

        final List<Map<String, String>> cassandraConfigsList = (List<Map<String, String>>) configDict.get("Cassandra");
        if (cassandraConfigsList != null)
            addCassandraNodes(records, hosts, cassandraConfigsList);

        final Map<String, ConfigValue> contactGroupList = (Map<String, ConfigValue>) configDict.get("Contacts");

        if (contactGroupList != null) {
            for(Map.Entry<String, ConfigValue> entry: contactGroupList.entrySet()) {
                addContactConfigs(entry.getKey(), ( List<Map<String, String>> ) entry.getValue());
            }
        }
    }

    private void addCassandraNodes(Records records, Hosts hosts, List<Map<String, String>> cassandraConfigsList) throws IOException {
        for (Map<String, String> cassandraConfigMap : cassandraConfigsList) {
            final RecordIdentifier recordIdentifier = RecordIdentifier.fromConfigMap(cassandraConfigMap);
            log.debug("Adding Cassandra from {} to {}", recordIdentifier, this);

            final CassandraCluster cluster = new CassandraCluster(hosts, records.getRecord(recordIdentifier));
            cassandraClusterSet.add(cluster);
            cluster.addToService(this);
        }
    }

    private void addContactConfigs(String contactGroup, List<Map<String, String>> contactConfigsList) throws IOException {
        for (Map<String, String> contactConfigMap : contactConfigsList) {
            if (contactMap.get(contactGroup) == null) {
                contactMap.put(contactGroup, new HashSet<Contact>());
            }
            String new_contact_email = contactConfigMap.get("Email");
            HashSet<Contact> cg = contactMap.get(contactGroup);

            cg.add(new Contact(contactConfigMap));
        }
    }

    private void addRecordConfigs(Records records, List<Map<String, String>> recordConfigsList) throws IOException {
        for (Map<String, String> recordConfigMap : recordConfigsList) {
            final RecordIdentifier recordIdentifier = RecordIdentifier.fromConfigMap(recordConfigMap);
            log.debug("Adding {} to {}", recordIdentifier, this);

            final Record record = records.getRecord(recordIdentifier);
            recordSet.add(record);
            record.addToService(this);
        }
    }

    @Getter(lazy = true)
    private final Set<Host> hostSet = grabHostSet();

    public Set<Host> grabHostSet() {
        final Set<Host> result = new HashSet<Host>();

        for (Record record : recordSet)
            result.addAll(record.getHostSet());

        for (CassandraCluster cluster : cassandraClusterSet)
            result.addAll(cluster.getHostSet());

        return result;
    }

    public Set<Host> getHostSetForSite(Site site) {
        final Set<Host> result = new HashSet<Host>();

        for (Record record : recordSet)
            result.addAll(record.getSiteSiteRecordMap().get(site).getHostSet());

        for (CassandraCluster cluster : cassandraClusterSet)
            result.addAll(cluster.getSiteCassandraSiteClusterMap().get(site).getHostSet());

        return result;
    }

    public Set<Host> getHostSetInSite(Site site) {
        final Set<Host> result = new HashSet<Host>();

        for (Record record : recordSet) {
            for (Host host : record.getHostSet()) {
                if (site.contains(host.getReverseName()))
                    result.add(host);
            }
        }

        for (CassandraCluster cluster : cassandraClusterSet) {
            for (Host host : cluster.getHostSet()) {
                if (site.contains(host.getReverseName()))
                    result.add(host);
            }
        }

        return result;
    }

    @Getter(lazy = true)
    private final Map<Site, Set<Host>> hostSetBySite = grabHostSetBySite();

    private Map<Site, Set<Host>> grabHostSetBySite() {
        Map<Site, Set<Host>> res = new HashMap<Site, Set<Host>>();

        for (Record record : recordSet) {
            for (Map.Entry<Site, SiteRecord> entry : record.getSiteSiteRecordMap().entrySet()) {
                final Set<Host> hostSet = entry.getValue().getHostSet();

                if (hostSet.size() != 0) {
                    if (!res.containsKey(entry.getKey()))
                        res.put(entry.getKey(), new HashSet<Host>());

                    res.get(entry.getKey()).addAll(hostSet);
                }
            }
        }

        for (CassandraCluster cluster : cassandraClusterSet) {
            for (Map.Entry<Site, CassandraSiteCluster> entry : cluster.getSiteCassandraSiteClusterMap().entrySet()) {
                final Set<Host> hostSet = entry.getValue().getHostSet();

                if (hostSet.size() != 0) {
                    if (!res.containsKey(entry.getKey()))
                        res.put(entry.getKey(), new HashSet<Host>());

                    res.get(entry.getKey()).addAll(hostSet);
                }
            }
        }

        return res;
    }
}
