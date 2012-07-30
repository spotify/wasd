package com.spotify.whoare.db;

import lombok.Data;

import java.util.Map;

@Data
public class RecordIdentifier {
        private final String name;
        private final Record.Type type;

        protected static RecordIdentifier fromConfigMap(Map<String, String> configMap) {
            final String name = configMap.get("Name");
            final Record.Type type;

            String typeName = configMap.get("Type");

            if (typeName.equals("SRV"))
                type = Record.Type.SRV;
            else if (typeName.equals("CNAME"))
                type = Record.Type.CNAME;
            else if (typeName.equals("A"))
                type = Record.Type.A;
            else
                type = Record.Type.SRV;

            return new RecordIdentifier(name, type);
        }
    }