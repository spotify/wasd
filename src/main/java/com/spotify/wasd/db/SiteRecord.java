package com.spotify.wasd.db;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@ToString(of = {"record", "site"}, includeFieldNames = false)
public class SiteRecord {
    @Getter
    private final Site site;
    @Getter
    private final com.spotify.wasd.db.Record record;
    @Getter
    private final Set<Host> hostSet;

    SiteRecord(com.spotify.wasd.db.Record parent, Site site, Hosts hosts, Resolver resolver) throws IOException {
        this.record = parent;
        this.site = site;

        hostSet = new HashSet<Host>();

        final int type;

        final RecordIdentifier id = record.getId();
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
            case PTR:
                type = Type.PTR;
                break;
            default:
                throw new IllegalArgumentException();
        }

        org.xbill.DNS.Record[] answers = null;

        try {
            final org.xbill.DNS.Record rec = org.xbill.DNS.Record.newRecord(name, type, DClass.IN);
            final Message message = Message.newQuery(rec);
            final Message resp = resolver.send(message);
            answers = resp.getSectionArray(Section.ANSWER);
        } catch (IOException e) {
            SiteRecord.log.warn("Failed retrieving {}", this);
        }

        if (answers == null) {
            log.info("No {} record {}", id.getType(), name);
            return;
        }

        log.debug("{} answers in {}", answers.length, this);

        for (org.xbill.DNS.Record answer : answers) {
            final String hostname;

            try {
                switch (id.getType()) {
                    case SRV:
                        hostname = ((SRVRecord) answer).getTarget().toString();
                        break;
                    case CNAME:
                        hostname = ((CNAMERecord) answer).getTarget().toString();
                        break;
                    case PTR:
                        hostname = ((PTRRecord) answer).getTarget().toString();
                        break;
                    case A:
                        hostname = ((ARecord) answer).getAddress().getCanonicalHostName();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown record type");
                }


                try {
                    final Host host = hosts.getHostByName(hostname);
                    hostSet.add(host);
                    host.addToSiteRecord(this);
                } catch (IOException e) {
                    log.error("Could not solve host {}", hostname);
                }
            } catch (ClassCastException e) {
                log.error("{} failed to grab answer: {}", this, e);
                throw e;
            }
        }
    }
}
