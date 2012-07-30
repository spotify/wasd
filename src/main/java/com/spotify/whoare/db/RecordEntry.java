package com.spotify.whoare.db;

import lombok.Data;

@Data
public class RecordEntry {
    private Record parent;
    private Host host;
}
