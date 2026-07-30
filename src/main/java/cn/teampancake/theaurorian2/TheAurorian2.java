package cn.teampancake.theaurorian2;

import com.mojang.logging.LogUtils;
import cn.teampancake.theaurorian2.common.crafting.AurorianRecipeIntegration;
import cn.teampancake.theaurorian2.common.enchantment.EnchantmentTooltips;
import cn.teampancake.theaurorian2.common.entity.TrainingDummyCommands;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModCreativeTabs;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModEnchantmentEffectTypes;
import cn.teampancake.theaurorian2.common.registry.ModFeatures;
import cn.teampancake.theaurorian2.common.registry.ModFluidTypes;
import cn.teampancake.theaurorian2.common.registry.ModFluids;
import cn.teampancake.theaurorian2.common.registry.ModItems;
import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import cn.teampancake.theaurorian2.common.network.ModNetworking;
import cn.teampancake.theaurorian2.common.registry.ModParticles;
import cn.teampancake.theaurorian2.common.registry.ModStats;
import cn.teampancake.theaurorian2.common.registry.ModStructures;
import cn.teampancake.theaurorian2.common.registry.ModTreeDecorators;
import cn.teampancake.theaurorian2.common.world.AurorianBlessingCycle;
import cn.teampancake.theaurorian2.mixin.FireBlockAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.transfer.fluid.DispenseFluidContainer;
import org.slf4j.Logger;

@Mod(TheAurorian2.MOD_ID)
public final class TheAurorian2 {

    public static final String MOD_ID = "theaurorian2";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceKey<DimensionType> AURORIAN_DIMENSION_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE, id("the_aurorian"));

    public TheAurorian2(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.register(modEventBus);
        ModFluidTypes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModEnchantmentEffectTypes.register(modEventBus);
        ModItems.register(modEventBus);
        ModTreeDecorators.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModStructures.register(modEventBus);
        ModParticles.register(modEventBus);
        ModStats.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(ModNetworking::registerPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(EnchantmentTooltips::onItemTooltip);
        NeoForge.EVENT_BUS.addListener(AurorianRecipeIntegration::onModifyRecipeJsons);
        NeoForge.EVENT_BUS.addListener(TrainingDummyCommands::register);
        NeoForge.EVENT_BUS.addListener(AurorianBlessingCycle::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onFluidPlaceBlock);
        NeoForge.EVENT_BUS.addListener(this::onBlockToolModification);
        modEventBus.addListener(this::commonSetup);
        LOGGER.info("Initializing The Aurorian 2");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(
                    ModBlocks.MOON_DEW_BUCKET.get(), DispenseFluidContainer.getInstance());
            ModStats.bootstrap();
            registerFlammableWood();
        });
    }

    private static void registerFlammableWood() {
        FireBlockAccessor fire = (FireBlockAccessor) Blocks.FIRE;
        registerFlammableWoodSet(fire, ModBlocks.SILENT_WOOD);
        registerFlammableWoodSet(fire, ModBlocks.CURTAIN_WOOD);
        registerFlammableWoodSet(fire, ModBlocks.CURSED_FROST_WOOD);
        fire.theaurorian2$setFlammable(ModBlocks.SILENT_TREE_LEAVES.get(), 30, 60);
        fire.theaurorian2$setFlammable(ModBlocks.CURTAIN_TREE_LEAVES.get(), 30, 60);
        fire.theaurorian2$setFlammable(ModBlocks.CURSED_FROST_TREE_LEAVES.get(), 30, 60);
        fire.theaurorian2$setFlammable(ModBlocks.SILENT_TREE_SAPLING.get(), 60, 100);
        fire.theaurorian2$setFlammable(ModBlocks.CURTAIN_TREE_SAPLING.get(), 60, 100);
        fire.theaurorian2$setFlammable(ModBlocks.CURSED_FROST_TREE_SAPLING.get(), 60, 100);
    }

    private static void registerFlammableWoodSet(FireBlockAccessor fire, ModBlocks.WoodSet wood) {
        fire.theaurorian2$setFlammable(wood.log().get(), 5, 5);
        fire.theaurorian2$setFlammable(wood.strippedLog().get(), 5, 5);
        fire.theaurorian2$setFlammable(wood.wood().get(), 5, 5);
        fire.theaurorian2$setFlammable(wood.strippedWood().get(), 5, 5);
        fire.theaurorian2$setFlammable(wood.planks().get(), 5, 20);
        fire.theaurorian2$setFlammable(wood.stairs().get(), 5, 20);
        fire.theaurorian2$setFlammable(wood.slab().get(), 5, 20);
        fire.theaurorian2$setFlammable(wood.fence().get(), 5, 20);
        fire.theaurorian2$setFlammable(wood.fenceGate().get(), 5, 20);
    }

    private void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
        if (event.getLevel() instanceof Level level
                && level.dimensionTypeRegistration().is(AURORIAN_DIMENSION_TYPE)
                && (event.getNewState().is(Blocks.STONE) || event.getNewState().is(Blocks.COBBLESTONE))) {
            event.setNewState(ModBlocks.AURORIAN_STONE.get().defaultBlockState());
        }
    }

    private void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() == ItemAbilities.AXE_STRIP) {
            ModBlocks.getStrippedState(event.getState()).ifPresent(event::setFinalState);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
