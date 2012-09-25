package com.spotify.wasd.db;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.cassandra.tools.NodeProbe;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@ToString(of = {"siteRecord"}, includeFieldNames = false)
public class CassandraSiteCluster {
    @Getter
    private final SiteRecord siteRecord;
    @Getter
    private final Set<Host> hostSet;
    @Getter
    private final CassandraCluster cassandraCluster;

    CassandraSiteCluster(final Hosts hosts, final SiteRecord siteRecord, CassandraCluster cassandraCluster) {
        this.siteRecord = siteRecord;
        this.cassandraCluster = cassandraCluster;
        Set<Host> readHostSet = null;

        if (siteRecord.getHostSet().size() == 0) {
            log.debug("Empty record for {}", this);
            this.hostSet = new HashSet<Host>();
            return;
        }

        for (Host node : siteRecord.getHostSet()) {
            try {
                log.debug("Contacting {} for {}", node, this);
                readHostSet = getHostSetFromNode(hosts, node);
                log.debug("{} worked for {}", node, this);
                break;
            } catch (IOException ignored) {
                log.warn("{} failed for {}", node, this);
            } catch (InterruptedException ignored) {
                log.warn("{} failed for {}", node, this);
            }
        }

        if (readHostSet == null) {
            log.error("Could not read {}", this);
            this.hostSet = new HashSet<Host>();
        } else {
            this.hostSet = readHostSet;
        }
    }

    final Set<Host> getHostSetFromNode(Hosts hosts, Host node) throws IOException, InterruptedException {
        final Set<Host> hostSet = new HashSet<Host>();

        NodeProbe probe = new NodeProbe(node.getReverseName());

        final Map<String, String> tokenToEndpointMap = probe.getTokenToEndpointMap();
        for (Map.Entry<String, String> entry : tokenToEndpointMap.entrySet()) {
            log.debug("{} has endpoint {}", this, entry.getValue());
            final Host host = hosts.getHostByName(entry.getValue());
            hostSet.add(host);
            host.addToCassandraSiteCluster(this);
        }

        probe.close();

        return hostSet;
    }
}
