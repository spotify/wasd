package com.spotify.whoare.db;

import lombok.Data;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class Records {
    private final Database database;
    private final List<Record> records;
    private final Map<RecordIdentifier, Record> identifierRecordMap;
    private final Resolver resolver;

    Records(Database database) throws UnknownHostException {
        this.database = database;
        resolver = new ExtendedResolver();
        records = new LinkedList<Record>();
        identifierRecordMap = new HashMap<RecordIdentifier, Record>();
    }

    protected final Record getRecord(RecordIdentifier id) throws TextParseException {
        final Record known = identifierRecordMap.get(id);
        if (known != null)
            return known;
        else {
            return getUnknownRecord(id);
        }
    }

    private Record getUnknownRecord(RecordIdentifier id) throws TextParseException {
        final Record record = new Record(id, database, resolver);
        records.add(record);
        identifierRecordMap.put(id, record);
        return (record);
    }
}
