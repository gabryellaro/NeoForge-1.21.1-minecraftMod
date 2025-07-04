package net.minemod.drone.item;

import net.minemod.drone.ModRegister;
import net.minemod.drone.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModRegister.MOD_ID);

    public static final DeferredItem<Item> SPAWN_DRONE = ITEMS.register("drone_spawn",
            () -> new DeferredSpawnEggItem(ModEntities.DRONE, 0x5C5C5C, 0xD3D3D3,
                    new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}

