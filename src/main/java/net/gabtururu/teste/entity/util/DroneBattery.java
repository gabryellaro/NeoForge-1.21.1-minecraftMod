package net.gabtururu.teste.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.gabtururu.teste.entity.custom.DroneEntity;

public class DroneBattery {
    private int level;
    private final int maxLevel;
    private int tickCounter = 0;
    private static final int TICK_INTERVAL = 5; // consome a cada 5 ticks

    public DroneBattery(int maxLevel) {
        this.level = maxLevel;
        this.maxLevel = maxLevel;
    }

    public int getLevel() {
        return level;
    }

    public int getPercent() {
        return (int)((level / (double) maxLevel) * 100);
    }

    public void recharge(int amount) {
        level = Math.min(maxLevel, level + amount);
    }

    public void consume(Level levelContext, DroneEntity drone) {
        tickCounter++;
        if (tickCounter < TICK_INTERVAL || this.level <= 0) return;
        tickCounter = 0;

        int drain = 0;

        Vec3 motion = drone.getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 0.0025) drain += 1;
        if (levelContext.isRainingAt(drone.blockPosition())) drain += 1;
        if (drone.getY() > 150) drain += 1;

        drain = Math.max(1, drain); // mínimo de 1

        level = Math.max(0, level - drain);
    }
}
