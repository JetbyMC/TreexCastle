package org.jetby.treexcastle.model;

import org.bukkit.Particle;
import org.bukkit.Sound;

public record FlyingDropParticle(
        Sound sound,
        float volume,
        float pitch,
        boolean visualFire,
        Particle particle,
        int amount,
        double offsetX,
        double offsetY,
        double offsetZ,
        double minY,
        double maxY,
        double minSpeed,
        double maxSpeed,
        int pickupDelay
) {

}
