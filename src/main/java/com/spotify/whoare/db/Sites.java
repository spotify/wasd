package com.spotify.whoare.db;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class Sites {
    private final Map<String, Site> aliasSiteMap;
    private final List<Site> siteList;

    Sites(Config config) {
        aliasSiteMap = new HashMap<String, Site>();
        siteList = new LinkedList<Site>();

        for (Map.Entry<String,ConfigValue> entry: config.root().entrySet()) {
            Site site = new Site(entry.getKey(), entry.getValue());

            siteList.add(site);

            aliasSiteMap.put(site.getName(), site);
            for (String alias: site.getAliasList())
                aliasSiteMap.put(alias, site);
        }
    }
}
