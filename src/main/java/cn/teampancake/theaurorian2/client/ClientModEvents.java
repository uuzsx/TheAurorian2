package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.color.AurorianGrassTintSource;
import cn.teampancake.theaurorian2.client.hud.AurorianNightHud;
import cn.teampancake.theaurorian2.client.hud.MoonShieldHud;
import cn.teampancake.theaurorian2.client.hud.SpiderMotherBossBar;
import cn.teampancake.theaurorian2.client.model.AurorianRabbitModel;
import cn.teampancake.theaurorian2.client.model.AurorianPigModel;
import cn.teampancake.theaurorian2.client.model.AurorianSheepModel;
import cn.teampancake.theaurorian2.client.model.AurorianCowModel;
import cn.teampancake.theaurorian2.client.particle.WickParticle;
import cn.teampancake.theaurorian2.client.particle.PhantomBloomPetalParticle;
import cn.teampancake.theaurorian2.client.particle.PhantomButterflyParticle;
import cn.teampancake.theaurorian2.client.particle.PhantomPetalParticle;
import cn.teampancake.theaurorian2.client.resource.AurorianGrassColorReloadListener;
import cn.teampancake.theaurorian2.client.renderer.DamageNumberRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianChestRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianChestMinecartRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianFurnaceRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianGrassRockRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianTableRenderer;
import cn.teampancake.theaurorian2.client.renderer.AstrologyTableRenderer;
import cn.teampancake.theaurorian2.client.renderer.CrystallineSwordPedestalRenderer;
import cn.teampancake.theaurorian2.client.renderer.ModelledBlockRenderer;
import cn.teampancake.theaurorian2.client.renderer.TrainingDummyRenderer;
import cn.teampancake.theaurorian2.client.renderer.SimpleGeoMobRenderer;
import cn.teampancake.theaurorian2.client.renderer.WallClimberSpiderlingRenderer;
import cn.teampancake.theaurorian2.client.renderer.SpiderVenomProjectileRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianRabbitRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianPigRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianSheepRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianCowRenderer;
import cn.teampancake.theaurorian2.client.screen.AstrologyForecastScreen;
import cn.teampancake.theaurorian2.common.network.AstrologyForecastPayload;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModFluidTypes;
import cn.teampancake.theaurorian2.common.registry.ModFluids;
import cn.teampancake.theaurorian2.common.registry.ModItems;
import cn.teampancake.theaurorian2.common.registry.ModParticles;
import cn.teampancake.theaurorian2.common.registry.ModStructureBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import org.joml.Vector4f;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.AURORIAN_RABBIT.get(), AurorianRabbitRenderer::new);
        event.registerEntityRenderer(ModEntities.AURORIAN_PIG.get(), AurorianPigRenderer::new);
        event.registerEntityRenderer(ModEntities.AURORIAN_SHEEP.get(), AurorianSheepRenderer::new);
        event.registerEntityRenderer(ModEntities.AURORIAN_COW.get(), AurorianCowRenderer::new);
        event.registerEntityRenderer(ModEntities.TRAINING_DUMMY.get(), TrainingDummyRenderer::new);
        event.registerEntityRenderer(
                ModEntities.SPIDER_MOTHER.get(),
                context -> new SimpleGeoMobRenderer<>(context, TheAurorian2.id("spider_mother"), 1.4F));
        event.registerEntityRenderer(
                ModEntities.SPIDERLING.get(),
                context -> new SimpleGeoMobRenderer<>(context, TheAurorian2.id("spiderling"), 0.35F));
        event.registerEntityRenderer(
                ModEntities.SPIDERLING_CRYSTAL_SHELL.get(),
                context -> new SimpleGeoMobRenderer<>(context, TheAurorian2.id("spiderling_crystal_shell"), 0.4F));
        event.registerEntityRenderer(
                ModEntities.SPIDERLING_WALL_CLIMBER.get(), WallClimberSpiderlingRenderer::new);
        event.registerEntityRenderer(
                ModEntities.SPIDER_EGG.get(),
                context -> new SimpleGeoMobRenderer<>(context, TheAurorian2.id("spider_egg"), 0.3F));
        event.registerEntityRenderer(
                ModEntities.SPIDER_VENOM.get(), SpiderVenomProjectileRenderer::new);
        event.registerEntityRenderer(
                ModEntities.SPIDER_SILK.get(), context -> new ThrownItemRenderer<>(context, 0.65F, true));
        event.registerEntityRenderer(ModEntities.DAMAGE_NUMBER.get(), DamageNumberRenderer::new);
        event.registerEntityRenderer(
                ModEntities.AURORIAN_CHEST_MINECART.get(), AurorianChestMinecartRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASTROLOGY_TABLE.get(), AstrologyTableRenderer::new);
        event.registerBlockEntityRenderer(
                ModBlockEntities.SACRIFICE_TABLE.get(),
                context -> new ModelledBlockRenderer<>(context, TheAurorian2.id("sacrifice_table")));
        event.registerBlockEntityRenderer(
                ModBlockEntities.CRYSTALLINE_SWORD_PEDESTAL.get(),
                CrystallineSwordPedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AURORIAN_CHEST.get(), AurorianChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AURORIAN_FURNACE.get(), AurorianFurnaceRenderer::new);
        event.registerBlockEntityRenderer(
                ModBlockEntities.AURORIAN_GRASS_ROCK.get(), AurorianGrassRockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AURORIAN_TABLE.get(), AurorianTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AURORIAN_SIGN.get(), StandingSignRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AURORIAN_HANGING_SIGN.get(), HangingSignRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SILENT_CAMPFIRE.get(), CampfireRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            NeoForge.EVENT_BUS.addListener(SpiderMotherBossBar::render);
            Sheets.addWoodType(ModStructureBlocks.SILENT_WOOD_TYPE);
            Sheets.addWoodType(ModStructureBlocks.WEEPING_WILLOW_WOOD_TYPE);
            Sheets.addWoodType(ModStructureBlocks.CURTAIN_WOOD_TYPE);
            Sheets.addWoodType(ModStructureBlocks.CURSED_FROST_WOOD_TYPE);
        });
    }

    @SubscribeEvent
    public static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(AstrologyForecastPayload.TYPE, (payload, context) ->
                Minecraft.getInstance().setScreen(new AstrologyForecastScreen(payload.forecast())));
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.WICK.get(), WickParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PHANTOM_BUTTERFLY_BLUE.get(), PhantomButterflyParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PHANTOM_BUTTERFLY_PINK.get(), PhantomButterflyParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PHANTOM_PETAL.get(), PhantomPetalParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PHANTOM_BLOOM_PETAL.get(), PhantomBloomPetalParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AurorianRabbitRenderer.LAYER, AurorianRabbitModel::createBodyLayer);
        event.registerLayerDefinition(AurorianPigRenderer.LAYER, AurorianPigModel::createBodyLayer);
        event.registerLayerDefinition(AurorianSheepRenderer.LAYER, AurorianSheepModel::createBodyLayer);
        event.registerLayerDefinition(AurorianCowRenderer.LAYER, AurorianCowModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.ARMOR_LEVEL,
                TheAurorian2.id("aurorian_night_hud"),
                AurorianNightHud::render);
        event.registerAbove(
                VanillaGuiLayers.ARMOR_LEVEL,
                TheAurorian2.id("moon_shield_hud"),
                MoonShieldHud::render);
    }

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        FluidModel.Unbaked moonDewModel = new FluidModel.Unbaked(
                new Material(Identifier.withDefaultNamespace("block/water_still")),
                new Material(Identifier.withDefaultNamespace("block/water_flow")),
                null,
                FluidTintSources.constant(0xFFFFFFFF));
        event.register(moonDewModel, ModFluids.MOON_DEW, ModFluids.FLOWING_MOON_DEW);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(
                    PoseStack poseStack,
                    LocalPlayer player,
                    HumanoidArm arm,
                    ItemStack itemInHand,
                    float partialTick,
                    float equipProcess,
                    float swingProcess) {
                  InteractionHand hand = player.getMainArm() == arm
                          ? InteractionHand.MAIN_HAND
                          : InteractionHand.OFF_HAND;
                  int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
                  if (player.isUsingItem() && player.getUsedItemHand() == hand) {
                      poseStack.translate(direction * 0.56F, -0.52F - equipProcess * 0.6F, -0.72F);
                      return true;
                  }

                  if (hand == InteractionHand.MAIN_HAND
                          && player.swinging
                          && player.swingingArm == hand
                          && swingProcess > 0.0F) {
                      poseStack.translate(direction * 0.56F, -0.52F - equipProcess * 0.6F, -0.72F);
                      float thrustOut = smoothProgress(progress(swingProcess, 0.06F, 0.22F));
                      float thrustReturn = smoothProgress(progress(swingProcess, 0.48F, 1.0F));
                      float thrust = thrustOut * (1.0F - thrustReturn);
                      poseStack.translate(0.0F, 0.0F, -0.5F * thrust);
                      return true;
                  }

                  return false;
              }

              private float progress(float value, float start, float end) {
                  return Math.clamp((value - start) / (end - start), 0.0F, 1.0F);
              }

              private float smoothProgress(float progress) {
                  return progress * progress * (3.0F - 2.0F * progress);
              }
          },
                ModItems.STARFORGED_KNIGHT_SPEAR.get(),
                ModItems.DAWNFORGED_KNIGHT_SPEAR.get(),
                ModItems.MOONFORGED_KNIGHT_SPEAR.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public void modifyFogColor(
                    Camera camera,
                    float partialTick,
                    ClientLevel level,
                    int renderDistance,
                    float darkenWorldAmount,
                    Vector4f fluidFogColor) {
                fluidFogColor.set(0.88F, 0.94F, 1.0F, 1.0F);
            }
        }, ModFluidTypes.MOON_DEW.get());
    }

    @SubscribeEvent
    public static void addClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(TheAurorian2.id("aurorian_grass_colormap"), new AurorianGrassColorReloadListener());
    }

    @SubscribeEvent
    public static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(
                List.of(AurorianGrassTintSource.INSTANCE),
                ModBlocks.AURORIAN_GRASS_BLOCK.get(),
                ModBlocks.AURORIAN_GRASS.get(),
                ModBlocks.AURORIAN_GRASS_LIGHT.get(),
                ModBlocks.TALL_AURORIAN_GRASS.get(),
                ModBlocks.TALL_AURORIAN_GRASS_LIGHT.get());
    }

    @SubscribeEvent
    public static void registerColorResolvers(RegisterColorHandlersEvent.ColorResolvers event) {
        event.register(AurorianGrassTintSource.COLOR_RESOLVER);
    }
}
