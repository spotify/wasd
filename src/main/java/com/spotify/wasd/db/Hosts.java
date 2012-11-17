package com.spotify.wasd.db;

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
    @Getter
    private final Set<String> unresolvedNamesSet;
    @Getter
    private final Set<Host> ringFailureHostsSet;

    private Resolver resolver;

    Hosts() throws UnknownHostException {
        hostSet = new HashSet<Host>();
        addrHostMap = new HashMap<InetAddress, Host>();
        nameHostMap = new HashMap<String, Host>();
        resolver = new ExtendedResolver();
        unresolvedNamesSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        ringFailureHostsSet = Collections.newSetFromMap(new ConcurrentHashMap<Host, Boolean>());
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
        if (host == null) {
            try {
                return getNewHostByName(name);
            } catch (IOException rethrown) {
                unresolvedNamesSet.add(name);
                throw rethrown;
            }
        }
        else
            return host;
    }

    PTRRecord getFirstPTRRecord(Record[] rrset) throws IOException {
        for (Record rr : rrset) {
            if (rr instanceof PTRRecord)
                return (PTRRecord) rr;
        }

        throw new IOException("No PTR record returned among " + rrset.length + " answers");
    }

    void reportFailedRingForHost(Host host) {
        ringFailureHostsSet.add(host);
    }

    Host getNewHostByAddr(InetAddress addr) throws IOException {
        final Host host;

        final Name reverseName = ReverseMap.fromAddress(addr);
        final Message resp = resolver.send(Message.newQuery(Record.newRecord(reverseName, Type.PTR, DClass.IN)));
        final PTRRecord answer = getFirstPTRRecord(resp.getSectionArray(Section.ANSWER));
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
