package com.spotify.wasd.db;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Sites {
    @Getter
    private final Map<String, Site> aliasSiteMap;
    @Getter
    private final HashSet<Site> siteSet;

    Sites(Config config) {
        aliasSiteMap = new HashMap<String, Site>();
        siteSet = new HashSet<Site>();

        for (Map.Entry<String, ConfigValue> entry : config.root().entrySet()) {
            Site site = new Site(entry.getKey(), entry.getValue());
            siteSet.add(site);
            aliasSiteMap.put(site.getName(), site);
            for (String alias : site.getAliasSet())
                aliasSiteMap.put(alias, site);
        }
    }
}
