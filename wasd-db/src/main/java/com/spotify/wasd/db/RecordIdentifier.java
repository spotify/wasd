package com.spotify.wasd.db;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class RecordIdentifier {
    @Getter
    private final String name;
    @Getter
    private final Record.Type type;

    static RecordIdentifier fromConfigMap(Map<String, String> configMap) {
        final String name = configMap.get("Name");
        final Record.Type type;

        String typeName = configMap.get("Type");

        if (typeName.equals("SRV"))
            type = Record.Type.SRV;
        else if (typeName.equals("CNAME"))
            type = Record.Type.CNAME;
        else if (typeName.equals("A"))
            type = Record.Type.A;
        else if (typeName.equals("PTR"))
            type = Record.Type.PTR;
        else
            type = Record.Type.SRV;

        return new RecordIdentifier(name, type);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        builder.append(" ");
        builder.append(name);
        return builder.toString();
    }
}