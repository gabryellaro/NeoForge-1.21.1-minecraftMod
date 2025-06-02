package net.gabtururu.teste.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class DroneEntity extends Mob {

    private static final TicketType<BlockPos> DRONE_TICKET = TicketType.create("drone_ticket", BlockPos::compareTo);

    private Vec3 targetPosition;
    private boolean moving = false;
    private BlockPos loadedChunkPos = null;

    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        if (moving && targetPosition != null) {
            Vec3 direction = targetPosition.subtract(position());
            double distance = direction.length();

            if (distance < 1.0) {
                stopMovement();
                return;
            }

            Vec3 directionNormalized = direction.normalize();
            directionNormalized = new Vec3(directionNormalized.x, directionNormalized.y * 0.2, directionNormalized.z);
            Vec3 velocity = directionNormalized.scale(0.3);
            setDeltaMovement(velocity);
            moveSelf();
        } else {
            // Hover a 1 bloco do chão
            double desiredHeight = 1.0;
            double currentY = getY();
            BlockPos groundPos = blockPosition().below();
            double groundY = groundPos.getY() + desiredHeight;
            double diff = groundY - currentY;
            double verticalSpeed = Math.max(Math.min(diff * 0.1, 0.05), -0.05);
            setDeltaMovement(new Vec3(0, verticalSpeed, 0));
            moveSelf();
        }
    }

    private void moveSelf() {
        move(MoverType.SELF, getDeltaMovement());
    }

    public void moveToPosition(Vec3 target) {
        this.targetPosition = target;
        this.moving = true;

        if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
            BlockPos current = blockPosition();
            BlockPos destination = new BlockPos((int) target.x, (int) target.y, (int) target.z);
            keepChunkLoaded(serverLevel, current);
            keepChunkLoaded(serverLevel, destination);
        }
    }

    private void keepChunkLoaded(ServerLevel level, BlockPos pos) {
        ChunkPos chunk = new ChunkPos(pos);
        level.getChunkSource().addRegionTicket(DRONE_TICKET, chunk, 1, pos);
        loadedChunkPos = pos;
    }

    private void stopMovement() {
        setDeltaMovement(Vec3.ZERO);
        moving = false;
        targetPosition = null;

        if (!level().isClientSide && level() instanceof ServerLevel serverLevel && loadedChunkPos != null) {
            ChunkPos chunk = new ChunkPos(loadedChunkPos);
            serverLevel.getChunkSource().removeRegionTicket(DRONE_TICKET, chunk, 1, loadedChunkPos);
            loadedChunkPos = null;
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.5);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        // Levanta o drone 1 bloco ao spawnar
        setPos(getX(), getY() + 1.0, getZ());
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }
}
