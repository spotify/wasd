package com.spotify.whoare.db;

import com.typesafe.config.ConfigValue;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Data
@ToString(exclude = "aliasList")
public class Site {
    private final String name;
    private final List<String> aliasList;

    protected Site(String name, ConfigValue configValue) {
        this.name = name;
        aliasList = new LinkedList<String>();
        final HashMap settings = (HashMap) configValue.unwrapped();
        if (settings != null) {
            List<String> aliases = (List<String>) settings.get("Aliases");
            if (aliases != null) {
                log.debug("Adding aliases {} to site {}", aliases, name);
                aliasList.addAll(aliases);
            }
            else
                log.warn("Site {} has no aliases", name);
        }
    }
}
