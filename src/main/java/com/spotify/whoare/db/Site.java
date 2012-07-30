package com.spotify.whoare.db;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.typesafe.config.ConfigValue;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@ToString(of = "name", includeFieldNames = false)
public class Site {
    @Getter
    private final String name;
    @Getter
    private final Set<String> aliasSet;

    Site(String name, ConfigValue configValue) {
        this.name = name;
        aliasSet = new HashSet<String>();
        final HashMap settings = (HashMap) configValue.unwrapped();
        if (settings != null) {
            List<String> aliases = (List<String>) settings.get("Aliases");
            if (aliases != null) {
                Site.log.debug("Adding aliases {} to site {}", aliases, name);
                aliasSet.addAll(aliases);
            } else
                Site.log.warn("Site {} has no aliases", name);
        }
    }

    boolean contains(String hostName) {
        final Optional<String> optShortName = FluentIterable.from(Splitter.on('.').split(hostName)).first();
        return optShortName.isPresent() && Joiner.on('.').join(optShortName.get(), name).equals(hostName);
    }
}
