package com.spotify.whoare.db;

import com.google.common.base.Joiner;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Data
@ToString(exclude = "hostSet")
public class SiteRecord {
    private final RecordIdentifier id;
    private final Site site;
    private final Set<Host> hostSet;

    protected SiteRecord(RecordIdentifier id, Site site, Database database, Resolver resolver) throws TextParseException {
        this.id = id;
        this.site = site;
        final Hosts hosts = database.getHosts();

        hostSet = new HashSet<Host>();

        final int type;

        final String idName = id.getName();
        final String siteName = site.getName();

        final Name name = Name.fromString(Joiner.on('.').join(idName, siteName));

        switch (id.getType()) {
            case SRV:
                type = Type.SRV;
                break;
            case CNAME:
                type = Type.CNAME;
                break;
            case A:
                type = Type.A;
                break;
            default:
                throw new NotImplementedException();
        }

        org.xbill.DNS.Record[] answers = null;

        try {
            final Record rec = Record.newRecord(name, type, DClass.IN);
            final Message message = Message.newQuery(rec);
            final Message resp = resolver.send(message);
            answers = resp.getSectionArray(Section.ANSWER);
            if (answers == null) {
                log.warn("No SRV record {}", name);
            }
        } catch (IOException e) {
            log.warn("Failed retrieving SRV record {}", name);
        }

        for (org.xbill.DNS.Record answer : answers) {
            SRVRecord record = (SRVRecord) answer;
            final String hostname = record.getTarget().toString();
            log.debug("Adding host {} in {}", hostname, this);
            try {
                hostSet.add(hosts.getHostByName(hostname));
            } catch (IOException e) {
                log.error("Could not solve host {}", hostname);
            }
        }
    }
}
