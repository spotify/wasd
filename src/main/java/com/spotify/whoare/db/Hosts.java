package com.spotify.whoare.db;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Slf4j
@Data
public class Hosts {
    private final Map<InetAddress, Host> addrHostMap;
    private final Map<String, Host> nameHostMap;
    private final Set<Host> hostSet;

    private Resolver resolver;

    protected Hosts() throws UnknownHostException {
        hostSet = new HashSet<Host>();
        addrHostMap = new HashMap<InetAddress, Host>();
        nameHostMap = new HashMap<String, Host>();
        resolver = new ExtendedResolver();
    }

    public Host getHostByAddr(InetAddress addr) throws IOException {
        final Host host = addrHostMap.get(addr);
        if (host == null)
            return getNewHostByAddr(addr);
        else
            return host;
    }

    public Host getHostByName(String name) throws IOException {
        final Host host = nameHostMap.get(name);
        if (host == null)
            return getNewHostByName(name);
        else
            return host;
    }

    private Host getNewHostByAddr(InetAddress addr) throws IOException {
        final Host host;

        final Name reverseName = ReverseMap.fromAddress(addr);
        final Message resp = resolver.send(Message.newQuery(Record.newRecord(reverseName, Type.PTR, DClass.IN)));

        final Record[] answers = resp.getSectionArray(Section.ANSWER);
        if (answers.length == 0)
            throw new IOException("DNS lookup failure");

        final PTRRecord answer = (PTRRecord) answers[0];
        final Name target = answer.getTarget();

        host = new Host(addr, target.toString());
        addHost(host);
        return host;
    }

    private Host getNewHostByName(String name) throws IOException {
        return getHostByAddr(Address.getByName(name));
    }

    private void addHost(Host host) {
        hostSet.add(host);
        addrHostMap.put(host.getAddress(), host);
        nameHostMap.put(host.getReverseName(), host);
    }
}
