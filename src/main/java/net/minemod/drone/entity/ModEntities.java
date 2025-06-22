package net.minemod.drone.entity;

import net.minemod.drone.TutorialMod;
import net.minemod.drone.entity.custom.DroneEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TutorialMod.MOD_ID);

    public static final Supplier<EntityType<DroneEntity>> DRONE =
                ENTITY_TYPES.register("drone", () -> EntityType.Builder.of(DroneEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.35f).build("drone"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}