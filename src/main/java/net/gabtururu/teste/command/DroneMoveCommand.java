package net.gabtururu.teste.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.gabtururu.teste.entity.custom.DroneEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DroneMoveCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("drone_move")
                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    double x = DoubleArgumentType.getDouble(ctx, "x");
                                                    double y = DoubleArgumentType.getDouble(ctx, "y");
                                                    double z = DoubleArgumentType.getDouble(ctx, "z");

                                                    CommandSourceStack source = ctx.getSource();
                                                    ServerLevel world = source.getLevel();
                                                    Vec3 target = new Vec3(x, y, z);

                                                    // Criar uma caixa de 50 blocos ao redor da posição do comando
                                                    Vec3 pos = source.getPosition();
                                                    AABB searchBox = new AABB(pos.x - 50, pos.y - 50, pos.z - 50, pos.x + 50, pos.y + 50, pos.z + 50);

                                                    // Encontra o drone mais próximo na área
                                                    DroneEntity nearest = world.getEntitiesOfClass(DroneEntity.class, searchBox)
                                                            .stream()
                                                            .findFirst()
                                                            .orElse(null);

                                                    if (nearest != null) {
                                                        nearest.moveToPosition(target);
                                                        source.sendSuccess(() -> Component.literal("Drone movendo para: " + target.x + ", " + target.y + ", " + target.z), false);
                                                        return 1;
                                                    } else {
                                                        source.sendFailure(Component.literal("Nenhum drone encontrado nas proximidades."));
                                                        return 0;
                                                    }
                                                })))));
    }
}
