package com.spotify.wasd.db;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

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
            } catch (IOException e) {
                hosts.reportFailedRingForHost(node);
                log.warn("{} failed for {}", node, this);
                log.warn("{}: {}", node, e);
            } catch (InterruptedException e) {
                hosts.reportFailedRingForHost(node);
                log.warn("{} failed for {}", node, this);
                log.warn("{}: {}", node, e);
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

        final TTransport transport = new TFramedTransport(new TSocket(node.getAddress().getHostAddress(), 9160));
        final TBinaryProtocol protocol = new TBinaryProtocol(transport);

        final Cassandra.Client client = new Cassandra.Client(protocol);

        try {
            Map<String, String> tokenToEndpointMap = client.describe_token_map();

            for (Map.Entry<String, String> entry : tokenToEndpointMap.entrySet()) {
                log.debug("{} has endpoint {}", this, entry.getValue());
                final Host host = hosts.getHostByName(entry.getValue());
                hostSet.add(host);
                host.addToCassandraSiteCluster(this);
            }
        } catch (InvalidRequestException e) {
            throw new IOException(e);
        } catch (TException e) {
            throw new IOException(e);
        } finally {
            transport.close();
        }

        return hostSet;
    }
}
