package net.minemod.drone;

import com.mojang.brigadier.CommandDispatcher;
import net.minemod.drone.command.DroneCommand;
import net.minemod.drone.command.WindCommand;
import net.minemod.drone.entity.ModEntities;
import net.minemod.drone.entity.client.DroneRenderer;
import net.minemod.drone.item.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(ModRegister.MOD_ID)
public class ModRegister {
    public static final String MOD_ID = "dronemod";
    private static final Logger LOGGER = LogUtils.getLogger();
    public ModRegister(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.SPAWN_EGGS){
            event.accept(ModItems.SPAWN_DRONE);
        }

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        MinecraftServer server = event.getServer();
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();

        DroneCommand.register(dispatcher);
        WindCommand.register(dispatcher);
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.DRONE.get(), DroneRenderer::new);
        }
    }
}
