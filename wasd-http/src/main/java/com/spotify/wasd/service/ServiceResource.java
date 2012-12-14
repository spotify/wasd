package com.spotify.wasd.service;

import com.spotify.wasd.db.*;
import com.sun.jersey.api.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnusedDeclaration")
@Slf4j
@Path("/services")
public class ServiceResource {

    private final DatabaseHolder holder;

    public ServiceResource(DatabaseHolder holder) {
        this.holder = holder;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getServices() {
        final JSONArray res = new JSONArray();
        for (Service srv : holder.current().getServices().getServiceSet())
            res.add(srv.getName());
        return res;
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getService(@PathParam("name") String name) {
        final Service service = holder.current().getServices().getNameServiceMap().get(name);

        if (service == null)
            throw new NotFoundException("No such service");

        final JSONArray res = new JSONArray();

        for (Host host : service.getHostSet())
            res.add(host.getReverseName());

        return res;
    }

    @GET
    @Path("/{name}/by_site")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getServiceForSite(@PathParam("name") String name) {
        final Database current = holder.current();
        final Service service = current.getServices().getNameServiceMap().get(name);

        if (service == null)
            throw new NotFoundException("No such service");

        final Map<Site, Set<Host>> hostSetBySite = service.getHostSetBySite();

        final JSONObject res = new JSONObject();
        for (Map.Entry<Site, Set<Host>> entry : hostSetBySite.entrySet()) {
            final JSONArray siteArray = new JSONArray();
            for (Host host : entry.getValue())
                siteArray.add(host.getReverseName());

            res.put(entry.getKey().getName(), siteArray);
        }

        return res;
    }

    @GET
    @Path("/{name}/for/{site}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getServiceForSite(@PathParam("name") String name, @PathParam("site") String siteName) {
        final Database current = holder.current();
        final Service service = current.getServices().getNameServiceMap().get(name);
        final Site site = current.getSites().getAliasSiteMap().get(siteName);

        if (service == null)
            throw new NotFoundException("No such service");
        if (site == null)
            throw new NotFoundException("No such site");

        final JSONArray res = new JSONArray();

        for (Host host : service.getHostSetForSite(site))
            res.add(host.getReverseName());

        return res;
    }


    @GET
    @Path("/{name}/in/{site}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getServiceInSite(@PathParam("name") String name, @PathParam("site") String siteName) {
        final Database current = holder.current();
        final Service service = current.getServices().getNameServiceMap().get(name);
        final Site site = current.getSites().getAliasSiteMap().get(siteName);

        if (service == null)
            throw new NotFoundException("No such service");
        if (site == null)
            throw new NotFoundException("No such site");

        final JSONArray res = new JSONArray();

        for (Host host : service.getHostSetInSite(site))
            res.add(host.getReverseName());

        return res;
    }
}
