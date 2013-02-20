package com.spotify.wasd.db;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(of={"email"})
public class Contact {
    public String name;
    public String email;
    public String type;

    Contact(Map<String, String> contactConfigMap ) {
        this.name = contactConfigMap.get("Name");
        this.email = contactConfigMap.get("Email");
        this.type = contactConfigMap.get("Type");
    }
}