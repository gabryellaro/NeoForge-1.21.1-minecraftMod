package net.gabtururu.teste.environment;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WindSystem {
    // Vento inicial: soprando do oeste para o leste (+X), como ventos reais
    private static Vec3 currentWind = new Vec3(1, 0, 0);
    private static double windStrength = 0.05; // força média do vento
    private static long lastUpdateTime = -1;
    private static boolean randomWindEnabled = false;

    public static void updateWind(Level level) {
        long time = level.getGameTime();

        if (randomWindEnabled && time % 1200 == 0 && time != lastUpdateTime && !level.isClientSide()) {
            RandomSource random = level.getRandom();
            double angle = random.nextDouble() * 2 * Math.PI;
            Vec3 newWind = new Vec3(Math.cos(angle), 0, Math.sin(angle)).normalize();
            double newStrength = 0.03 + (random.nextDouble() * 0.07);

            setWind(newWind, newStrength, (ServerLevel) level);
            lastUpdateTime = time;
        }
    }

    public static Vec3 getWindDirection() {
        return currentWind.normalize();
    }

    public static double getWindStrength() {
        return windStrength;
    }

    public static void setWind(Vec3 direction, double strength, ServerLevel level) {
        currentWind = direction.normalize();
        windStrength = strength;

        String directionName = getWindDirectionName(currentWind);
        Component msg = Component.literal("§b[Clima] O vento agora sopra para " + directionName + " com força " + String.format("%.2f", windStrength));
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(msg);
        }
    }

    public static void enableRandomWind(boolean enabled, ServerLevel level) {
        randomWindEnabled = enabled;
        Component msg = Component.literal("§e[Clima] Vento randômico " + (enabled ? "ativado." : "desativado."));
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(msg);
        }
    }

    private static String getWindDirectionName(Vec3 vec) {
        double angle = Math.toDegrees(Math.atan2(-vec.z, vec.x));
        if (angle < 0) angle += 360;

        if (angle >= 337.5 || angle < 22.5) return "Leste";
        else if (angle < 67.5) return "Nordeste";
        else if (angle < 112.5) return "Norte";
        else if (angle < 157.5) return "Noroeste";
        else if (angle < 202.5) return "Oeste";
        else if (angle < 247.5) return "Sudoeste";
        else if (angle < 292.5) return "Sul";
        else return "Sudeste";
    }
}
