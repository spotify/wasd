package com.spotify.whoare.db;

import lombok.Getter;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Resolver;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Records {
    private final Resolver resolver;
    @Getter
    private final List<Record> recordList;
    @Getter
    private final Map<RecordIdentifier, Record> identifierRecordMap;

    private final Sites sites;
    private final Hosts hosts;

    Records(Sites sites, Hosts hosts) throws UnknownHostException {
        this.sites = sites;
        this.hosts = hosts;
        resolver = new ExtendedResolver();
        recordList = new LinkedList<Record>();
        identifierRecordMap = new HashMap<RecordIdentifier, Record>();
    }

    final Record getRecord(RecordIdentifier id) throws IOException {
        final Record known = identifierRecordMap.get(id);
        if (known != null)
            return known;
        else {
            return getUnknownRecord(id);
        }
    }

    private Record getUnknownRecord(RecordIdentifier id) throws IOException {
        final Record record = new Record(id, sites, hosts, resolver);
        recordList.add(record);
        identifierRecordMap.put(id, record);
        return (record);
    }
}
