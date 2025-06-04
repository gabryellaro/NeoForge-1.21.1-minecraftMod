package net.gabtururu.teste.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class DroneEntity extends Mob {

    private static final TicketType<BlockPos> DRONE_TICKET = TicketType.create("drone_ticket", BlockPos::compareTo);

    private Vec3 targetPosition;
    private boolean moving = false;
    private BlockPos loadedChunkPos = null;

    private boolean returningToRecharge = false;
    private BlockPos rechargeStation = null;

    private int batteryLevel = 1200;

    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        int oldBatteryLevel = batteryLevel;

        if (!returningToRecharge && batteryLevel < 200) {
            Optional<BlockPos> nearestRecharge = findNearestRedstoneBlock(blockPosition(), 30);
            if (nearestRecharge.isPresent()) {
                targetPosition = Vec3.atCenterOf(nearestRecharge.get());
                returningToRecharge = true;
                rechargeStation = nearestRecharge.get();
                moving = true;
            }
        }

        if (moving && targetPosition != null) {
            if (batteryLevel <= 0) {
                stopMovement();
                this.setNoGravity(false);
                return;
            }

            batteryLevel--;

            Vec3 direction = targetPosition.subtract(position());
            double distance = direction.length();

            if (distance < 1.0) {
                if (returningToRecharge && rechargeStation != null && blockPosition().closerThan(rechargeStation, 2)) {
                    rechargeBattery(1200);
                    returningToRecharge = false;
                }
                stopMovement();
                return;
            }

            Vec3 forward = direction.normalize().scale(0.5);
            Vec3 ahead = position().add(forward);
            BlockPos checkPos = new BlockPos((int) Math.floor(ahead.x), (int) Math.floor(ahead.y), (int) Math.floor(ahead.z));
            BlockState blockAhead = level().getBlockState(checkPos);
            boolean isBlocked = !blockAhead.isAir();

            double verticalDiff = targetPosition.y - getY();
            double verticalSpeed = Math.max(Math.min(verticalDiff * 0.05, 0.15), -0.15);

            if (isBlocked) {
                verticalSpeed = 0.2;
            }

            Vec3 horizontalDir = new Vec3(direction.x, 0, direction.z).normalize().scale(0.3);
            Vec3 velocity = new Vec3(horizontalDir.x, verticalSpeed, horizontalDir.z);

            setDeltaMovement(velocity);
            moveSelf();
        } else {
            double desiredHeight = 1.0;
            double currentY = getY();
            BlockPos groundPos = blockPosition().below();
            double groundY = groundPos.getY() + desiredHeight;
            double diff = groundY - currentY;
            double verticalSpeed = Math.max(Math.min(diff * 0.1, 0.05), -0.05);
            setDeltaMovement(new Vec3(0, verticalSpeed, 0));
            moveSelf();
        }

        BlockPos under = blockPosition().below();
        if (level().getBlockState(under).is(Blocks.REDSTONE_BLOCK)) {
            rechargeBattery(5);
        }

        if (batteryLevel <= 0) {
            BlockPos belowPos = this.blockPosition().below();
            BlockState blockBelow = level().getBlockState(belowPos);
            boolean isWater = blockBelow.getFluidState().isSource();

            if (!this.onGround() && !isWater) {
                // força movimento descendente manual mesmo com noGravity = true
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(new Vec3(motion.x, Math.max(motion.y - 0.05, -0.15), motion.z));
                this.move(MoverType.SELF, this.getDeltaMovement());
            } else {
                // parou no chão ou em cima da água
                this.setDeltaMovement(Vec3.ZERO);
            }

        }


        if (tickCount % 20 == 0) {
            updateBatteryName();
        }

        if (batteryLevel != oldBatteryLevel) {
            if (batteryLevel <= 200) {
                sendMessageToNearestPlayer(Component.literal("§cBateria baixa! Apenas " + batteryLevel + " ticks restantes."));
            } else if (batteryLevel % 200 == 0) {
                sendMessageToNearestPlayer(Component.literal("Drone bateria: " + batteryLevel + " ticks restantes."));
            }
        }
    }

    private Optional<BlockPos> findNearestRedstoneBlock(BlockPos origin, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos check = origin.offset(dx, dy, dz);
                    if (level().getBlockState(check).getBlock() == Blocks.REDSTONE_BLOCK) {
                        return Optional.of(check);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void sendMessageToNearestPlayer(Component message) {
        Player nearestPlayer = level().getNearestPlayer(blockPosition().getX(), blockPosition().getY(), blockPosition().getZ(), 50, false);
        if (nearestPlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message);
        }
    }

    private void moveSelf() {
        move(MoverType.SELF, getDeltaMovement());
    }

    public void moveToPosition(Vec3 target) {
        this.targetPosition = target;
        this.moving = true;
        this.returningToRecharge = false;

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

    private void rechargeBattery(int amount) {
        batteryLevel = Math.min(1200, batteryLevel + amount);
    }

    private void updateBatteryName() {
        String bar = getBatteryBar();
        this.setCustomNameVisible(true);
        this.setCustomName(Component.literal("Bateria: " + bar));
    }

    private String getBatteryBar() {
        int totalBars = 10;
        int filledBars = (int) ((batteryLevel / 1200.0) * totalBars);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            bar.append(i < filledBars ? "█" : "░");
        }
        return bar.toString();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.FLYING_SPEED, 1.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        setPos(getX(), getY() + 1.0, getZ());
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }
}
