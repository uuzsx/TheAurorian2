package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.color.AurorianGrassTintSource;
import cn.teampancake.theaurorian2.client.hud.AurorianNightHud;
import cn.teampancake.theaurorian2.client.particle.WickParticle;
import cn.teampancake.theaurorian2.client.resource.AurorianGrassColorReloadListener;
import cn.teampancake.theaurorian2.client.renderer.DamageNumberRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianChestRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianFurnaceRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianGrassRockRenderer;
import cn.teampancake.theaurorian2.client.renderer.AurorianTableRenderer;
import cn.teampancake.theaurorian2.client.renderer.AstrologyTableRenderer;
import cn.teampancake.theaurorian2.client.renderer.TrainingDummyRenderer;
import cn.teampancake.theaurorian2.client.screen.AstrologyForecastScreen;
import cn.teampancake.theaurorian2.common.network.AstrologyForecastPayload;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModFluidTypes;
import cn.teampancake.theaurorian2.common.registry.ModFluids;
import cn.teampancake.theaurorian2.common.registry.ModParticles;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.joml.Vector4f;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TRAINING_DUMMY.get(), TrainingDummyRenderer::new);
        event.registerEntityRenderer(ModEntities.DAMAGE_NUMBER.get(), DamageNumberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASTROLOGY_TABLE.get(), AstrologyTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AURORIAN_CHEST.get(), AurorianChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AURORIAN_FURNACE.get(), AurorianFurnaceRenderer::new);
        event.registerBlockEntityRenderer(
                ModBlockEntities.AURORIAN_GRASS_ROCK.get(), AurorianGrassRockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AURORIAN_TABLE.get(), AurorianTableRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(AstrologyForecastPayload.TYPE, (payload, context) ->
                Minecraft.getInstance().setScreen(new AstrologyForecastScreen(payload.forecast())));
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.WICK.get(), WickParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.ARMOR_LEVEL,
                TheAurorian2.id("aurorian_night_hud"),
                AurorianNightHud::render);
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
