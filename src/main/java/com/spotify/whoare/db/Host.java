package com.spotify.whoare.db;

import lombok.Data;

import java.net.InetAddress;

@Data
public class Host {
    private final InetAddress address;
    private final String reverseName;
}
