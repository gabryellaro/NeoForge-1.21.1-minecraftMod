package net.minemod.drone.entity.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface Drone {
    void moveToPosition(Vec3 target);
    void setBatteryLevel(int percent);
    void setBatteryCapacity(int capacity);
    boolean isSmart();
    void setSmart(boolean value);
    void stopMovement();

    BlockPos blockPosition();
    boolean onGround();
    void setDeltaMovement(Vec3 vec);
    Vec3 getDeltaMovement();
    Vec3 position();
    boolean isWindFavorable();
    java.util.Optional<BlockPos> findNearestRedstoneBlock(BlockPos from);

    // Se necessário, adicione getters/setters para targetPosition, moving, etc.
    Vec3 getTargetPosition();
    void setTargetPosition(Vec3 pos);
    boolean isMoving();
    void setMoving(boolean moving);
    net.minecraft.world.level.Level level();

    void tick();
}