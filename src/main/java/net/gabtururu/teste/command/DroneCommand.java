package net.gabtururu.teste.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.gabtururu.teste.entity.custom.DroneEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DroneCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("drone")
                        .then(Commands.literal("move")
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            double x = DoubleArgumentType.getDouble(ctx, "x");
                                                            double y = DoubleArgumentType.getDouble(ctx, "y");
                                                            double z = DoubleArgumentType.getDouble(ctx, "z");

                                                            return withNearestDrone(ctx.getSource(), drone -> {
                                                                drone.moveToPosition(new Vec3(x, y, z));
                                                                ctx.getSource().sendSuccess(() -> Component.literal("Drone movendo para: " + x + ", " + y + ", " + z), false);
                                                            });
                                                        })))))
                        .then(Commands.literal("set")
                                .then(Commands.literal("batteryCapacity")
                                        .then(Commands.argument("capacity", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    int capacity = IntegerArgumentType.getInteger(ctx, "capacity");

                                                    return withNearestDrone(ctx.getSource(), drone -> {
                                                        drone.setBatteryCapacity(capacity);
                                                        ctx.getSource().sendSuccess(() -> Component.literal("Capacidade da bateria definida para: " + capacity), false);
                                                    });
                                                })))
                                .then(Commands.literal("battery")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    int level = IntegerArgumentType.getInteger(ctx, "level");

                                                    return withNearestDrone(ctx.getSource(), drone -> {
                                                        drone.setBatteryLevel(level);
                                                        ctx.getSource().sendSuccess(() -> Component.literal("Nível da bateria definido para: " + level), false);
                                                    });
                                                }))))
        );
    }

    private static int withNearestDrone(CommandSourceStack source, java.util.function.Consumer<DroneEntity> action) {
        ServerLevel world = source.getLevel();
        Vec3 pos = source.getPosition();
        AABB searchBox = new AABB(pos.x - 50, pos.y - 50, pos.z - 50, pos.x + 50, pos.y + 50, pos.z + 50);

        DroneEntity nearest = world.getEntitiesOfClass(DroneEntity.class, searchBox)
                .stream()
                .findFirst()
                .orElse(null);

        if (nearest != null) {
            action.accept(nearest);
            return 1;
        } else {
            source.sendFailure(Component.literal("Nenhum drone encontrado nas proximidades."));
            return 0;
        }
    }
}