package com.spotify.whoare.db;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class Hosts {
    @Getter
    private final Map<InetAddress, Host> addrHostMap;
    @Getter
    private final Map<String, Host> nameHostMap;
    @Getter
    private final Set<Host> hostSet;

    private Resolver resolver;

    Hosts() throws UnknownHostException {
        hostSet = new HashSet<Host>();
        addrHostMap = new HashMap<InetAddress, Host>();
        nameHostMap = new HashMap<String, Host>();
        resolver = new ExtendedResolver();
    }

    Host getHostByAddr(InetAddress addr) throws IOException {
        final Host host = addrHostMap.get(addr);
        if (host == null)
            return getNewHostByAddr(addr);
        else
            return host;
    }

    Host getHostByName(String name) throws IOException {
        final Host host = nameHostMap.get(name);
        if (host == null)
            return getNewHostByName(name);
        else
            return host;
    }

    Host getNewHostByAddr(InetAddress addr) throws IOException {
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

    public final Set<Host> getHostsByPrefix(String name) {
        Set<Host> result = new HashSet<Host>();

        if (name == null)
            return getHostSet();

        for (Map.Entry<String, Host> entry : nameHostMap.entrySet())
            if (entry.getKey().startsWith(name))
                result.add(entry.getValue());

        return result;
    }
}
