package net.gabtururu.teste.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.gabtururu.teste.entity.custom.DroneEntity;

public class DroneBattery {
    private int level;
    private final int maxLevel;
    private int tickCounter = 0;
    private static final int TICK_INTERVAL = 5;

    public DroneBattery(int maxLevel) {
        this.level = maxLevel;
        this.maxLevel = maxLevel;
    }

    public int getLevel() {
        return level;
    }

    public int getCapacity() {
        return maxLevel;
    }

    public int getPercent() {
        return (int)((level / (double) maxLevel) * 100);
    }

    public void setPercent(int percent) {
        percent = Math.max(0, Math.min(100, percent));
        this.level = (int) ((percent / 100.0) * this.maxLevel);
    }

    public void recharge(int amount) {
        level = Math.min(maxLevel, level + amount);
    }

    public void tickRecharge(Level levelContext, DroneEntity drone) {
        BlockPos under = drone.blockPosition().below();
        boolean isRedstone = levelContext.getBlockState(under).is(Blocks.REDSTONE_BLOCK);

        if (isRedstone && drone.getDeltaMovement().lengthSqr() < 0.002) {
            recharge(1);
        }
    }

    public void consume(Level levelContext, DroneEntity drone, double multiplier) {
        tickCounter++;
        if (tickCounter < TICK_INTERVAL || this.level <= 0) return;
        tickCounter = 0;

        int drain = 0;

        Vec3 motion = drone.getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 0.0025) drain += 1;
        if (levelContext.isRainingAt(drone.blockPosition())) drain += 2;
        if (levelContext.isThundering() && levelContext.isRainingAt(drone.blockPosition())) {
            drain += 3;
        }
        if (drone.getY() > 150) drain += 1;

        drain = Math.max(1, drain);

        // Aplica o modificador do vento
        drain = (int) Math.ceil(drain * multiplier);

        level = Math.max(0, level - drain);
    }
}
