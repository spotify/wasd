package com.spotify.wasd.db;

import lombok.Getter;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Resolver;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Records {
    private final Resolver resolver;
    @Getter
    private final Set<Record> recordSet;
    @Getter
    private final Map<RecordIdentifier, Record> identifierRecordMap;

    private final Sites sites;
    private final Hosts hosts;

    Records(Sites sites, Hosts hosts) throws UnknownHostException {
        this.sites = sites;
        this.hosts = hosts;
        resolver = new ExtendedResolver();
        recordSet = Collections.newSetFromMap(new ConcurrentHashMap<Record, Boolean>());
        identifierRecordMap = new ConcurrentHashMap<RecordIdentifier, Record>();
    }

    final Record getRecord(RecordIdentifier id) throws IOException {
        synchronized (identifierRecordMap) {
            final Record known = identifierRecordMap.get(id);
            if (known != null)
                return known;
            else {
                return getUnknownRecord(id);
            }
        }
    }

    /* always call from synchronized(identifierRecordMap)  */
    private Record getUnknownRecord(RecordIdentifier id) throws IOException {
        final Record record = new Record(id, sites, hosts, resolver);
        recordSet.add(record);
        identifierRecordMap.put(id, record);
        return (record);
    }
}
