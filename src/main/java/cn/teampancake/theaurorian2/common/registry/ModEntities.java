package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.DamageNumberEntity;
import cn.teampancake.theaurorian2.common.entity.AurorianChestMinecartEntity;
import cn.teampancake.theaurorian2.common.entity.CrystalShellSpiderlingEntity;
import cn.teampancake.theaurorian2.common.entity.SpiderEggEntity;
import cn.teampancake.theaurorian2.common.entity.SpiderMotherEntity;
import cn.teampancake.theaurorian2.common.entity.SpiderSilkProjectileEntity;
import cn.teampancake.theaurorian2.common.entity.SpiderVenomProjectileEntity;
import cn.teampancake.theaurorian2.common.entity.SpiderlingEntity;
import cn.teampancake.theaurorian2.common.entity.TrainingDummyEntity;
import cn.teampancake.theaurorian2.common.entity.IserynValeEntity;
import cn.teampancake.theaurorian2.common.entity.WallClimberSpiderlingEntity;
import cn.teampancake.theaurorian2.common.entity.PurificationRiftEntity;
import cn.teampancake.theaurorian2.common.entity.PurificationRitualZombieEntity;
import cn.teampancake.theaurorian2.common.entity.AurorianRabbitEntity;
import cn.teampancake.theaurorian2.common.entity.AurorianPigEntity;
import cn.teampancake.theaurorian2.common.entity.AurorianSheepEntity;
import cn.teampancake.theaurorian2.common.entity.AurorianCowEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {

    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(TheAurorian2.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<AurorianRabbitEntity>> AURORIAN_RABBIT =
            ENTITIES.registerEntityType(
                    "aurorian_rabbit", AurorianRabbitEntity::new, MobCategory.CREATURE,
                    builder -> builder.sized(0.4F, 0.5F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<AurorianPigEntity>> AURORIAN_PIG =
            ENTITIES.registerEntityType(
                    "aurorian_pig", AurorianPigEntity::new, MobCategory.CREATURE,
                    builder -> builder.sized(0.9F, 0.9F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<AurorianSheepEntity>> AURORIAN_SHEEP =
            ENTITIES.registerEntityType(
                    "aurorian_sheep", AurorianSheepEntity::new, MobCategory.CREATURE,
                    builder -> builder.sized(1.0F, 1.3F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<AurorianCowEntity>> AURORIAN_COW =
            ENTITIES.registerEntityType(
                    "aurorian_cow", AurorianCowEntity::new, MobCategory.CREATURE,
                    builder -> builder.sized(1.5F, 1.46F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<IserynValeEntity>> ISERYN_VALE =
            ENTITIES.registerEntityType(
                    "iseryn_vale",
                    IserynValeEntity::new,
                    MobCategory.CREATURE,
                    builder -> builder
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .clientTrackingRange(10)
                            .noLootTable());

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

    public static final DeferredHolder<EntityType<?>, EntityType<SpiderMotherEntity>> SPIDER_MOTHER =
            ENTITIES.registerEntityType(
                    "spider_mother",
                    SpiderMotherEntity::new,
                    MobCategory.MONSTER,
                    builder -> builder
                            .fireImmune()
                            .sized(3.0F, 2.5F)
                            .eyeHeight(1.65F)
                            .clientTrackingRange(12)
                            .updateInterval(2));
    public static final DeferredHolder<EntityType<?>, EntityType<SpiderlingEntity>> SPIDERLING =
            ENTITIES.registerEntityType(
                    "spiderling",
                    SpiderlingEntity::new,
                    MobCategory.MONSTER,
                    builder -> builder
                            .sized(0.9F, 0.55F)
                            .eyeHeight(0.35F)
                            .clientTrackingRange(8)
                            .noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalShellSpiderlingEntity>>
            SPIDERLING_CRYSTAL_SHELL = ENTITIES.registerEntityType(
                    "spiderling_crystal_shell",
                    CrystalShellSpiderlingEntity::new,
                    MobCategory.MONSTER,
                    builder -> builder
                            .sized(1.0F, 0.65F)
                            .eyeHeight(0.4F)
                            .clientTrackingRange(8)
                            .noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<WallClimberSpiderlingEntity>>
            SPIDERLING_WALL_CLIMBER = ENTITIES.registerEntityType(
                    "spiderling_wall_climber",
                    WallClimberSpiderlingEntity::new,
                    MobCategory.MONSTER,
                    builder -> builder
                            .sized(1.2F, 0.6F)
                            .eyeHeight(0.3F)
                            .clientTrackingRange(10)
                            .noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<SpiderEggEntity>> SPIDER_EGG =
            ENTITIES.registerEntityType(
                    "spider_egg",
                    SpiderEggEntity::new,
                    MobCategory.MISC,
                    builder -> builder
                            .sized(0.55F, 0.65F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<SpiderVenomProjectileEntity>> SPIDER_VENOM =
            ENTITIES.registerEntityType(
                    "spider_venom",
                    SpiderVenomProjectileEntity::new,
                    MobCategory.MISC,
                    builder -> builder
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<SpiderSilkProjectileEntity>> SPIDER_SILK =
            ENTITIES.registerEntityType(
                    "spider_silk",
                    SpiderSilkProjectileEntity::new,
                    MobCategory.MISC,
                    builder -> builder
                            .sized(0.3F, 0.3F)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .noLootTable());

    public static final DeferredHolder<EntityType<?>, EntityType<PurificationRiftEntity>> PURIFICATION_RIFT =
            ENTITIES.registerEntityType(
                    "purification_rift",
                    PurificationRiftEntity::new,
                    MobCategory.MISC,
                    builder -> builder
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .noSave()
                            .noSummon()
                            .noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<PurificationRitualZombieEntity>>
            PURIFICATION_RITUAL_ZOMBIE = ENTITIES.registerEntityType(
                    "purification_ritual_zombie",
                    PurificationRitualZombieEntity::new,
                    MobCategory.MONSTER,
                    builder -> builder
                            .sized(0.6F, 1.95F)
                            .eyeHeight(1.74F)
                            .clientTrackingRange(12)
                            .updateInterval(2)
                            .noSave()
                            .noLootTable());

    public static final DeferredHolder<EntityType<?>, EntityType<AurorianChestMinecartEntity>>
            AURORIAN_CHEST_MINECART = ENTITIES.registerEntityType(
                    "aurorian_chest_minecart",
                    AurorianChestMinecartEntity::new,
                    MobCategory.MISC,
                    builder -> builder
                            .noLootTable()
                            .sized(0.98F, 0.7F)
                            .passengerAttachments(0.1875F)
                            .clientTrackingRange(8));

    private ModEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(ModEntities::registerSpawnPlacements);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TRAINING_DUMMY.get(), TrainingDummyEntity.createAttributes().build());
        event.put(SPIDER_MOTHER.get(), SpiderMotherEntity.createAttributes().build());
        event.put(SPIDERLING.get(), SpiderlingEntity.createAttributes().build());
        event.put(SPIDERLING_CRYSTAL_SHELL.get(), CrystalShellSpiderlingEntity.createAttributes().build());
        event.put(SPIDERLING_WALL_CLIMBER.get(), WallClimberSpiderlingEntity.createAttributes().build());
        event.put(SPIDER_EGG.get(), SpiderEggEntity.createAttributes().build());
        event.put(AURORIAN_RABBIT.get(), Rabbit.createAttributes().build());
        event.put(AURORIAN_PIG.get(), Pig.createAttributes().build());
        event.put(AURORIAN_SHEEP.get(), Sheep.createAttributes().build());
        event.put(AURORIAN_COW.get(), AbstractCow.createAttributes().build());
        event.put(ISERYN_VALE.get(), IserynValeEntity.createAttributes().build());
        event.put(PURIFICATION_RITUAL_ZOMBIE.get(),
                net.minecraft.world.entity.monster.zombie.Zombie.createAttributes().build());
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        registerAnimalSpawn(event, AURORIAN_RABBIT.get());
        registerAnimalSpawn(event, AURORIAN_PIG.get());
        registerAnimalSpawn(event, AURORIAN_SHEEP.get());
        registerAnimalSpawn(event, AURORIAN_COW.get());
    }

    private static <T extends Animal> void registerAnimalSpawn(
            RegisterSpawnPlacementsEvent event, EntityType<T> type) {
        event.register(
                type,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
