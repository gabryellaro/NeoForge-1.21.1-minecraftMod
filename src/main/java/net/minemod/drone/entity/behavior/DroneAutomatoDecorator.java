package net.minemod.drone.entity.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minemod.drone.entity.util.DroneBattery;

public class DroneAutomatoDecorator implements Drone {
    private static final int CRITICAL_BATTERY_PERCENT = 10;

    private final Drone drone;
    private final DroneBattery battery;

    public DroneAutomatoDecorator(Drone drone, DroneBattery battery) {
        this.drone = drone;
        this.battery = battery;
        this.drone.setSmart(true);
    }

    @Override
    public void moveToPosition(Vec3 target) {
        drone.moveToPosition(target);
    }

    @Override
    public void setBatteryLevel(int percent) {
        drone.setBatteryLevel(percent);
    }

    @Override
    public void setBatteryCapacity(int capacity) {
        drone.setBatteryCapacity(capacity);
    }

    @Override
    public boolean isSmart() {
        return drone.isSmart();
    }

    @Override
    public void setSmart(boolean value) {
        drone.setSmart(value);
    }

    @Override
    public void stopMovement() {
        drone.stopMovement();
    }

    @Override
    public BlockPos blockPosition() {
        return drone.blockPosition();
    }

    @Override
    public boolean onGround() {
        return drone.onGround();
    }

    @Override
    public void setDeltaMovement(Vec3 vec) {
        drone.setDeltaMovement(vec);
    }

    @Override
    public Vec3 getDeltaMovement() {
        return drone.getDeltaMovement();
    }

    @Override
    public Vec3 position() {
        return drone.position();
    }

    @Override
    public boolean isWindFavorable() {
        return drone.isWindFavorable();
    }

    @Override
    public java.util.Optional<BlockPos> findNearestRedstoneBlock(BlockPos from) {
        return drone.findNearestRedstoneBlock(from);
    }

    @Override
    public Vec3 getTargetPosition() {
        return drone.getTargetPosition();
    }

    @Override
    public void setTargetPosition(Vec3 pos) {
        drone.setTargetPosition(pos);
    }

    @Override
    public boolean isMoving() {
        return drone.isMoving();
    }

    @Override
    public void setMoving(boolean moving) {
        drone.setMoving(moving);
    }

    @Override
    public net.minecraft.world.level.Level level() {
        return drone.level();
    }

    @Override
    public void tick() {
        handleCriticalBatteryBehavior();

        if (drone.isMoving()) {
            Vec3 target = drone.getTargetPosition();
            Vec3 pos = drone.position();

            if (target != null && pos.distanceTo(target) > 0.5) {
                Vec3 direction = target.subtract(pos).normalize().scale(0.1);
                drone.setDeltaMovement(direction);
            } else {
                drone.setMoving(false);
                drone.setDeltaMovement(Vec3.ZERO);
            }
        }
    }

    public boolean tryDivertFromWaterOnLowBattery() {
        for (int i = 1; i <= 6; i++) {
            BlockPos pos = drone.blockPosition().below(i);
            if (drone.level().getBlockState(pos).getFluidState().isSource()) {
                BlockPos safePos = null;

                outer:
                for (int dx = -7; dx <= 7; dx++) {
                    for (int dz = -7; dz <= 7; dz++) {
                        for (int dy = 0; dy <= 4; dy++) {
                            BlockPos check = drone.blockPosition().offset(dx, -1 + dy, dz);
                            BlockState state = drone.level().getBlockState(check);
                            boolean isSolid = state.isSolid();
                            boolean notWater = !state.getFluidState().isSource();

                            if (isSolid && notWater) {
                                safePos = check.above();
                                break outer;
                            }
                        }
                    }
                }

                if (safePos != null) {
                    drone.setTargetPosition(Vec3.atCenterOf(safePos));
                    drone.setMoving(true);
                    return true;
                }

                break;
            }
        }
        return false;
    }


    public void handleCriticalBatteryFall() {
        drone.stopMovement();
        BlockPos belowPos = drone.blockPosition().below();
        BlockState blockBelow = drone.level().getBlockState(belowPos);
        boolean isWater = blockBelow.getFluidState().isSource();

        if (!drone.onGround() && !isWater) {
            Vec3 motion = drone.getDeltaMovement();
            drone.setDeltaMovement(new Vec3(motion.x, Math.max(motion.y - 0.05, -0.15), motion.z));
        } else {
            drone.setDeltaMovement(Vec3.ZERO);
        }
    }

    public boolean shouldInitiateCriticalBatteryDescent() {
        int criticalLevel = (int) (battery.getCapacity() * (CRITICAL_BATTERY_PERCENT / 100.0));

        if (battery.getLevel() > criticalLevel || drone.findNearestRedstoneBlock(drone.blockPosition()).isPresent()) {
            return false;
        }

        if (!drone.isSmart()) {
            return true;
        }

        boolean windFavorable = drone.isWindFavorable();
        Vec3 target = drone.getTargetPosition();

        boolean nearTarget = target != null && drone.position().distanceTo(target) < 15.0;

        return !(nearTarget && windFavorable);
    }

    public void handleCriticalBatteryBehavior() {
        if (!shouldInitiateCriticalBatteryDescent()) return;

        if (drone.isSmart()) {
            if (!tryDivertFromWaterOnLowBattery()) {
                handleCriticalBatteryFall();
            }
        } else {
            handleCriticalBatteryFall();
        }
    }


}
