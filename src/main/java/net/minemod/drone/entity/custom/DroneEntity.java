package net.minemod.drone.entity.custom;

import net.minecraft.world.damagesource.DamageTypes;
import net.minemod.drone.entity.behavior.Drone;
import net.minemod.drone.entity.behavior.DroneAutomatoDecorator;
import net.minemod.drone.entity.util.DroneBattery;
import net.minemod.drone.environment.WindSystem;
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

public class DroneEntity extends Mob implements Drone {
    private DroneBattery battery = new DroneBattery(MAX_BATTERY);
    private DroneAutomatoDecorator automatoDecorator;

    public final AnimationState flyAnimationState = new AnimationState();
    private int flyAnimationTimeout = 0;

    // Ticket para manter chunks carregados enquanto o drone se move
    private static final TicketType<BlockPos> DRONE_TICKET = TicketType.create("drone_ticket", BlockPos::compareTo);

    // Constantes de bateria
    private static final int MAX_BATTERY = 100;
    private static final int LOW_BATTERY_PERCENT = 20;
    private static final int CRITICAL_BATTERY_PERCENT = 10; // 20%

    private Vec3 targetPosition;
    private boolean moving = false;
    private BlockPos loadedChunkPos = null;
    private int lastBatteryPercentWarned = -1;

    private boolean returningToRecharge = false;
    private BlockPos rechargeStation = null;
    private boolean smart = true;

    public void setSmart(boolean value) {
        this.smart = value;
    }

    public boolean isSmart() {
        return smart;
    }

    @Override
    public Vec3 getTargetPosition() {
        return this.targetPosition;
    }

    @Override
    public void setTargetPosition(Vec3 target) {
        this.targetPosition = target;
    }

    @Override
    public boolean isMoving() {
        return this.moving;
    }

    @Override
    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public void setBatteryCapacity(int capacity) {
        int currentPercent = battery.getPercent();
        this.battery = new DroneBattery(capacity);
        this.automatoDecorator = new DroneAutomatoDecorator(this, this.battery);
        this.battery.setPercent(currentPercent);
    }

    public void setBatteryLevel(int percent) {
        this.battery.setPercent(percent);
    }

    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.automatoDecorator = new DroneAutomatoDecorator(this, this.battery);
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
            setupAnimationStates();
            return;
        }

        if (this.smart && automatoDecorator != null) {
            automatoDecorator.handleCriticalBatteryBehavior();
        } else {
            handleCriticalBatteryBehavior(); // fallback para modo não inteligente
        }

        handleLowBatteryReturn();
        handleMovementLogic();
        handleHoveringWhenIdle();
        rechargeIfOnRedstoneBlock();
        handleCriticalBatteryBehavior();
        updateBatteryNamePeriodically();
        sendBatteryWarningMessages();
    }


    private void handleLowBatteryReturn() {
        int batteryPercent = battery.getPercent();
        if (!returningToRecharge && batteryPercent < LOW_BATTERY_PERCENT) {
            Optional<BlockPos> nearestRecharge = findNearestRedstoneBlock(blockPosition());
            if (nearestRecharge.isPresent()) {
                BlockPos pos = nearestRecharge.get();
                targetPosition = new Vec3(pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5);
                returningToRecharge = true;
                rechargeStation = nearestRecharge.get();
                moving = true;
            }
        }
    }

    private void handleMovementLogic() {
        if (moving && targetPosition != null) {
            if (battery.getLevel() <= 0) {
                stopMovement();
                this.setNoGravity(false);
                return;
            }

            battery.consume(level(), this, calculateWindFactor());

            Vec3 directionToTarget = targetPosition.subtract(position());
            double distance = directionToTarget.length();

            if (distance < 0.5) {
                if (returningToRecharge && rechargeStation != null && blockPosition().closerThan(rechargeStation, 2)) {
                    rechargeBattery(MAX_BATTERY);
                    returningToRecharge = false;
                } else {
                }
                stopMovement();
                return;
            }

            WindSystem.updateWind(level());

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
        }
    }

    private void handleHoveringWhenIdle() {
        if (!moving) {
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

    private void rechargeIfOnRedstoneBlock() {
        BlockPos under = blockPosition().below();
        if (level().getBlockState(under).is(Blocks.REDSTONE_BLOCK)) {
            rechargeBattery(2);
        }
    }

    private void handleCriticalBatteryBehavior() {
        if (!shouldInitiateCriticalBatteryDescent()) return;

        handleCriticalBatteryFall();
    }

    private boolean shouldInitiateCriticalBatteryDescent() {
        int criticalLevel = (int) (battery.getCapacity() * (CRITICAL_BATTERY_PERCENT / 100.0));

        if (battery.getLevel() > criticalLevel || findNearestRedstoneBlock(blockPosition()).isPresent()) {
            return false;
        }

        if (!smart) {
            return true;
        }

        boolean windFavorable = isWindFavorable();
        boolean nearTarget = this.targetPosition != null && this.position().distanceTo(this.targetPosition) < 15.0;

        return !(nearTarget && windFavorable);
    }

    private void handleCriticalBatteryFall() {
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

    private void updateBatteryNamePeriodically() {
        if (tickCount % 20 == 0) {
            updateBatteryName();
        }
    }

    private void sendBatteryWarningMessages() {
        int batteryPercent = battery.getPercent();
        if (batteryPercent <= LOW_BATTERY_PERCENT && batteryPercent % 10 == 0 && batteryPercent != lastBatteryPercentWarned) {
            sendMessageToNearestPlayer(Component.literal("§cBateria baixa! " + batteryPercent + "% restante."));
            lastBatteryPercentWarned = batteryPercent;
        } else if (batteryPercent > LOW_BATTERY_PERCENT && batteryPercent % 10 == 0 && batteryPercent != lastBatteryPercentWarned) {
            sendMessageToNearestPlayer(Component.literal("Drone bateria: " + batteryPercent + "%."));
            lastBatteryPercentWarned = batteryPercent;
        }
    }


    // Busca bloco de redstone mais próximo para recarga
    public Optional<BlockPos> findNearestRedstoneBlock(BlockPos origin) {
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

    // Envia mensagem ao jogador mais próximo
    private void sendMessageToNearestPlayer(Component message) {
        Player nearestPlayer = level().getNearestPlayer(blockPosition().getX(), blockPosition().getY(), blockPosition().getZ(), 50, false);
        if (nearestPlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message);
        }
    }

    // Move a entidade usando o tipo SELF
    private void moveSelf() {
        move(MoverType.SELF, getDeltaMovement());
    }

    // Inicia movimento para uma posição alvo
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

    public void stopMovement() {
        setDeltaMovement(Vec3.ZERO);
        moving = false;
        targetPosition = null;

        if (!level().isClientSide && level() instanceof ServerLevel serverLevel && loadedChunkPos != null) {
            ChunkPos chunk = new ChunkPos(loadedChunkPos);
            serverLevel.getChunkSource().removeRegionTicket(DRONE_TICKET, chunk, 1, loadedChunkPos);
            loadedChunkPos = null;
        }
    }

    // Recarrega a bateria do drone
    private void rechargeBattery(int amount) {
        battery.tickRecharge(level(), this);
    }

    // Gera barra visual de bateria
    private String getBatteryBar(int percent) {
        int totalBars = 10;
        double percentPerBar = 100.0 / totalBars;
        int fullBars = (int) (percent / percentPerBar);
        boolean hasHalfBar = (percent % percentPerBar) >= (percentPerBar / 2);

        // Define a cor da barra inteira conforme o percentual
        String colorCode;
        if (percent > 50) {
            colorCode = "§a"; // Verde-claro
        } else if (percent > 20) {
            colorCode = "§e"; // Amarelo
        } else {
            colorCode = "§c"; // Vermelho
        }

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < totalBars; i++) {
            bar.append(colorCode);  // Aplica a cor antes do caractere

            if (i < fullBars) {
                bar.append("█");
            } else if (i == fullBars && hasHalfBar) {
                bar.append("▒");
            } else {
                bar.append(" ");
            }
        }
        bar.append("§r]"); // Reseta a cor ao final da barra

        return bar.toString();
    }

    private double calculateWindFactor() {
        if (targetPosition == null) return 1.0;

        Vec3 movementDir = targetPosition.subtract(position()).normalize();
        Vec3 windDir = WindSystem.getWindDirection();
        double windStrength = WindSystem.getWindStrength();

        double dot = movementDir.dot(windDir); // -1 (contra), 0 (lateral), 1 (a favor)
        double adjustment = 1.0 - (dot * windStrength);
        return Math.max(0.5, Math.min(1.5, adjustment));
    }

    public boolean isWindFavorable() {
        if (targetPosition == null) return false;

        Vec3 movementDir = targetPosition.subtract(position()).normalize();
        Vec3 windDir = WindSystem.getWindDirection();
        double dot = movementDir.dot(windDir);

        return dot > 0.7; // vento está empurrando o drone para frente
    }

    // Atualiza o nome customizado do drone com a barra de bateria
    private void updateBatteryName() {
        double percent = ((double) battery.getLevel() / battery.getCapacity()) * 100;
        percent = Math.min(percent, 100); // Garante que nunca ultrapasse 100%

        String bar = getBatteryBar((int) percent);
        this.setCustomNameVisible(true);
        this.setCustomName(Component.literal("Bateria: " + bar + " " + (int) percent + "%"));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.FALLING_BLOCK)) {
            return false;
        }
        return super.hurt(source, amount);
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
