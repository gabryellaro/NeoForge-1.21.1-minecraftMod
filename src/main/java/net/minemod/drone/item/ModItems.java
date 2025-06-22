package net.minemod.drone.item;

import net.minemod.drone.TutorialMod;
import net.minemod.drone.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    //Como registrar itens, todos devem ser public static final
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TutorialMod.MOD_ID);

    //aqui é o primeiro item registrado com um id que deve ser identificavel
    //o nome dentro do registro deve ser uppercase como tudo que tem no mine
    public static final DeferredItem<Item> SPAWN_DRONE = ITEMS.register("drone_egg",
            () -> new DeferredSpawnEggItem(ModEntities.DRONE, 0x31afaf, 0xffac00,
                    new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}

