package com.spotify.wasd.service;

import com.spotify.wasd.db.DatabaseHolder;
import com.spotify.wasd.db.Host;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Slf4j
@Path("/find/hosts")
public class FindHostResource {
    private final DatabaseHolder holder;

    public FindHostResource(DatabaseHolder holder) {
        this.holder = holder;
    }

    @GET
    @Path("/unresolved")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getUnresolvedNames() {
        JSONArray namesArray = new JSONArray();

        Set<String> namesList = holder.current().getHosts().getUnresolvedNamesSet();
        for (String name : namesList) {
            namesArray.add(name);
        }

        return namesArray;
    }

    @GET
    @Path("/ring_failures")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getRingFailures() {
        JSONArray namesArray = new JSONArray();

        Set<Host> hostsList = holder.current().getHosts().getRingFailureHostsSet();
        for (Host host : hostsList) {
            namesArray.add(host.getReverseName());
        }

        return namesArray;
    }
}
