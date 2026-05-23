package org.jetby.treexcastle.model;

import java.util.List;

public record Holo(
        boolean enable,
        double holoX,
        double holoY,
        double holoZ,
        List<String> lines
) {
}
