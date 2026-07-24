package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.DamageNumberEntity;
import cn.teampancake.theaurorian2.common.entity.TrainingDummyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {

    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(TheAurorian2.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<TrainingDummyEntity>> TRAINING_DUMMY =
            ENTITIES.registerEntityType(
                    "training_dummy",
                    TrainingDummyEntity::new,
                    MobCategory.MISC,
                    builder -> builder
                            .sized(0.75F, 1.94F)
                            .eyeHeight(1.7F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .noLootTable());

    public static final DeferredHolder<EntityType<?>, EntityType<DamageNumberEntity>> DAMAGE_NUMBER =
            ENTITIES.registerEntityType(
                    "damage_number",
                    DamageNumberEntity::new,
                    MobCategory.MISC,
                    builder -> builder
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .noSave()
                            .noSummon()
                            .noLootTable());

    private ModEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
        modEventBus.addListener(ModEntities::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TRAINING_DUMMY.get(), TrainingDummyEntity.createAttributes().build());
    }
}
