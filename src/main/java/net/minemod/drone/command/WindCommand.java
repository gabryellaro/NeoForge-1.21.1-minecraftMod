package net.minemod.drone.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minemod.drone.environment.WindSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class WindCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wind")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("direction", StringArgumentType.word())
                                        .then(Commands.argument("strength", DoubleArgumentType.doubleArg(0.01, 1.0))
                                                .executes(ctx -> {
                                                    String dir = StringArgumentType.getString(ctx, "direction");
                                                    double strength = DoubleArgumentType.getDouble(ctx, "strength");
                                                    ServerLevel level = ctx.getSource().getLevel();
                                                    Vec3 direction = parseDirection(dir);

                                                    if (direction == null) {
                                                        ctx.getSource().sendFailure(Component.literal("§cDireção inválida. Use north, south, east, west, northeast, northwest, southeast, southwest."));
                                                        return 0;
                                                    }

                                                    WindSystem.setWind(direction, strength, level);
                                                    WindSystem.enableRandomWind(false, level);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("random")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean enable = BoolArgumentType.getBool(ctx, "enabled");
                                            ServerLevel level = ctx.getSource().getLevel();

                                            WindSystem.enableRandomWind(enable, level);
                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static Vec3 parseDirection(String dir) {
        return switch (dir.toLowerCase()) {
            case "north" -> new Vec3(0, 0, -1);
            case "south" -> new Vec3(0, 0, 1);
            case "east" -> new Vec3(1, 0, 0);
            case "west" -> new Vec3(-1, 0, 0);
            case "northeast" -> new Vec3(1, 0, -1);
            case "northwest" -> new Vec3(-1, 0, -1);
            case "southeast" -> new Vec3(1, 0, 1);
            case "southwest" -> new Vec3(-1, 0, 1);
            default -> null;
        };
    }
}
