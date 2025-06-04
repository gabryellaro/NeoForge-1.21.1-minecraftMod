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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class DroneEntity extends Mob {

    private static final TicketType<BlockPos> DRONE_TICKET = TicketType.create("drone_ticket", BlockPos::compareTo);

    private Vec3 targetPosition;
    private boolean moving = false;
    private BlockPos loadedChunkPos = null;

    private int batteryLevel = 1200; // máx. 1200 ticks (1 minuto)

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

        // Se estiver se movendo, gasta bateria
        if (moving && targetPosition != null) {
            if (batteryLevel <= 0) {
                stopMovement();
                return;
            }

            batteryLevel--;

            Vec3 direction = targetPosition.subtract(position());
            double distance = direction.length();

            if (distance < 1.0) {
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

        // Verifica recarga em bloco de redstone
        BlockPos under = blockPosition().below();
        if (level().getBlockState(under).is(Blocks.REDSTONE_BLOCK)) {
            rechargeBattery(5);
        }

        // Atualiza nome da bateria a cada segundo
        if (tickCount % 20 == 0) {
            updateBatteryName();
        }

        // Envia mensagem no chat de 200 em 200 ticks, e aviso de bateria baixa em 200 ou menos
        if (batteryLevel != oldBatteryLevel) {
            if (batteryLevel <= 200) {
                sendMessageToNearestPlayer(Component.literal("§cBateria baixa! Apenas " + batteryLevel + " ticks restantes."));
            } else if (batteryLevel % 200 == 0) {
                sendMessageToNearestPlayer(Component.literal("Drone bateria: " + batteryLevel + " ticks restantes."));
            }
        }
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
