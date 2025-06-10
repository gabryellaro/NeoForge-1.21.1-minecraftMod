package net.gabtururu.teste.entity.custom;

import net.gabtururu.teste.entity.util.DroneBattery;
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
    private DroneBattery battery = new DroneBattery(MAX_BATTERY);
    public final AnimationState flyAnimationState = new AnimationState();
    private int flyAnimationTimeout = 0;

    private static final TicketType<BlockPos> DRONE_TICKET = TicketType.create("drone_ticket", BlockPos::compareTo);

    private static final int MAX_BATTERY = 100;
    private static final double LOW_BATTERY_THRESHOLD = 0.20; // 20%

    private Vec3 targetPosition;
    private boolean moving = false;
    private BlockPos loadedChunkPos = null;
    private int lastBatteryPercentWarned = -1;

    private boolean returningToRecharge = false;
    private BlockPos rechargeStation = null;


    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    private void setupAnimationStates() {
        if (this.flyAnimationTimeout <= 0) {
            this.flyAnimationTimeout = 4;
            this.flyAnimationState.start(this.tickCount);
        } else {
            --this.flyAnimationTimeout;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
            return; // Só anima no cliente
        }

        double batteryPercentage = battery.getLevel() / (double) MAX_BATTERY;

        if (!returningToRecharge && batteryPercentage < LOW_BATTERY_THRESHOLD) {
            Optional<BlockPos> nearestRecharge = findNearestRedstoneBlock(blockPosition());
            if (nearestRecharge.isPresent()) {
                targetPosition = Vec3.atCenterOf(nearestRecharge.get());
                returningToRecharge = true;
                rechargeStation = nearestRecharge.get();
                moving = true;
            }
        }

        if (moving && targetPosition != null) {
            if (battery.getLevel() <= 0) {
                stopMovement();
                this.setNoGravity(false);
                return;
            }

            battery.consume(level(), this);

            Vec3 directionToTarget = targetPosition.subtract(position());
            double distance = directionToTarget.length();

            if (distance < 1.0) {
                if (returningToRecharge && rechargeStation != null && blockPosition().closerThan(rechargeStation, 2)) {
                    rechargeBattery(MAX_BATTERY);
                    returningToRecharge = false;
                }
                stopMovement();
                return;
            }

            Vec3 waterInfluence = Vec3.ZERO;
            int waterCount = 0;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    BlockPos posBelow = blockPosition().offset(dx, -1, dz);
                    BlockState state = level().getBlockState(posBelow);

                    if (state.getFluidState().isSource()) {
                        waterInfluence = waterInfluence.add(new Vec3(dx, 0, dz));
                        waterCount++;
                    }
                }
            }

            Vec3 finalDirection;
            if (waterCount > 0) {
                Vec3 avgWaterDir = waterInfluence.scale(1.0 / waterCount).normalize();
                finalDirection = directionToTarget.normalize().scale(0.6).add(avgWaterDir.scale(0.4)).normalize();
            } else {
                finalDirection = directionToTarget.normalize();
            }

            Vec3 forward = finalDirection.scale(0.5);
            Vec3 ahead = position().add(forward);
            BlockPos checkPos = new BlockPos((int) Math.floor(ahead.x), (int) Math.floor(ahead.y), (int) Math.floor(ahead.z));
            BlockState blockAhead = level().getBlockState(checkPos);
            boolean isBlocked = !blockAhead.isAir();

            double verticalDiff = targetPosition.y - getY();
            double verticalSpeed = Math.max(Math.min(verticalDiff * 0.05, 0.15), -0.15);

            if (isBlocked) {
                verticalSpeed = 0.2;
            }

            Vec3 horizontalDir = new Vec3(finalDirection.x, 0, finalDirection.z).normalize().scale(0.3);
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

        if (battery.getLevel() <= 10 && !findNearestRedstoneBlock(blockPosition()).isPresent()) {
            stopMovement();
            BlockPos belowPos = this.blockPosition().below();
            BlockState blockBelow = level().getBlockState(belowPos);
            boolean isWater = blockBelow.getFluidState().isSource();

            if (!this.onGround() && !isWater) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(new Vec3(motion.x, Math.max(motion.y - 0.05, -0.15), motion.z));
            } else {
                this.setDeltaMovement(Vec3.ZERO);
            }
        }

        if (tickCount % 20 == 0) {
            updateBatteryName();
        }

        int batteryPercent = battery.getPercent();
        if (batteryPercent <= 20 && batteryPercent % 10 == 0 && batteryPercent != lastBatteryPercentWarned) {
            sendMessageToNearestPlayer(Component.literal("§cBateria baixa! " + batteryPercent + "% restante."));
            lastBatteryPercentWarned = batteryPercent;
        } else if (batteryPercent > 20 && batteryPercent % 10 == 0 && batteryPercent != lastBatteryPercentWarned) {
            sendMessageToNearestPlayer(Component.literal("Drone bateria: " + batteryPercent + "%."));
            lastBatteryPercentWarned = batteryPercent;
        }

    }

    private Optional<BlockPos> findNearestRedstoneBlock(BlockPos origin) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -10; dx <= 10; dx++) {
            for (int dy = -10; dy <= 10; dy++) {
                for (int dz = -10; dz <= 10; dz++) {
                    mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (level().getBlockState(mutable).is(Blocks.REDSTONE_BLOCK)) {
                        return Optional.of(mutable.immutable()); // preserve immutability
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
        battery.recharge(amount);
    }

    private String getBatteryBar(int percent) {
        int totalBars = 10;
        int filledBars = (int) ((percent / 100.0) * totalBars);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < totalBars; i++) {
            bar.append(i < filledBars ? "█" : " ");
        }
        bar.append("]");
        return bar.toString();
    }

    private void updateBatteryName() {
        int percent = (int) ((battery.getLevel() / (double) MAX_BATTERY) * 100);
        String bar = getBatteryBar(percent);
        this.setCustomNameVisible(true);
        this.setCustomName(Component.literal("Bateria: " + bar + " " + percent + "%"));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FLYING_SPEED, 0.5);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        setPos(getX(), getY(), getZ());
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }
}
