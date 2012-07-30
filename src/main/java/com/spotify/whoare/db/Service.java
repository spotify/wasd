package com.spotify.whoare.db;

import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import lombok.Data;
import lombok.Getter;
import org.xbill.DNS.TextParseException;

import java.util.*;

@Data
public class Service {
    private final String name;
    private final List<Record> recordList;

    protected Service(String name, ConfigValue configValue, Database database) throws TextParseException {
        this.name = name;
        recordList = new LinkedList<Record>();
        final Map<String, ConfigObject> configDict = (Map<String, ConfigObject>) configValue.unwrapped();
        final List<Map<String, String>> recordConfigsList = (List<Map<String, String>>) configDict.get("Records");
        for (Map<String, String> recordConfigMap : recordConfigsList) {
            recordList.add(database.getRecords().getRecord(RecordIdentifier.fromConfigMap(recordConfigMap)));
        }
    }

    @Getter(lazy = true)
    private final Set<Host> hosts = getHostSet();

    private Set<Host> getHostSet() {
        final Set<Host> result = new HashSet<Host>();

        for (Record record : recordList) {
            result.addAll(record.getHosts());
        }
        return result;
    }

    private Set<Host> getHostSet(Site site) {
        final Set<Host> result = new HashSet<Host>();
        for (Record record: recordList) {
            result.addAll(record.getZoneRecords().get(site).getHostSet());
        }
        return result;
    }
}
