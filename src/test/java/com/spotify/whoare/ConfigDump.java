package com.spotify.whoare;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import java.util.List;
import java.util.Map;

public class ConfigDump {
    public static void main(String[] args) {
        final Config config = ConfigFactory.load();
        final Config scan = config.getConfig("Scan");
        final List<String> backward = scan.getStringList("Backward");

        for (String ipPrefix: backward) {
            System.out.println(ipPrefix);
        }

        final ConfigObject forward = scan.getObject("Forward");

        for (Map.Entry<String,ConfigValue> entry: forward.entrySet()) {
            System.out.printf("%s: %s\n", entry.getKey().toString(), entry.getValue().toString());
        }
    }
}
