package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.DataTickets;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoObjectRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.GeckoLibUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class WarblerShoulderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final ContextKey<Byte> SHOULDERS = new ContextKey<>(TheAurorian2.id("warbler_shoulders"));
    // One animation cache per layer, not a client entity per player or per frame.
    private final PerchedBird bird = new PerchedBird();
    private final GeoObjectRenderer<PerchedBird, AvatarRenderState, GeoRenderState> renderer =
            new GeoObjectRenderer<>(new DefaultedEntityGeoModel<>(TheAurorian2.id("azure_warbler"))) {
                @Override
                public void adjustRenderPose(RenderPassInfo<GeoRenderState> pass) {
                    // Attached at the feet: do not apply GeoObjectRenderer's block-center offset.
                }

                @Override
                public void addRenderData(PerchedBird bird, AvatarRenderState player, GeoRenderState state, float partialTick) {
                    state.addGeckolibData(DataTickets.PACKED_LIGHT, player.lightCoords);
                    state.addGeckolibData(DataTickets.OUTLINE_COLOR, player.outlineColor);
                }
            };

    public WarblerShoulderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            var playerRenderer = event.getPlayerRenderer(skin);
            if (playerRenderer != null) playerRenderer.addLayer(new WarblerShoulderLayer(playerRenderer));
        }
    }

    @SubscribeEvent
    public static void registerRenderData(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                byte mask = avatar instanceof AbstractClientPlayer player
                        ? player.getExistingData(ModAttachments.AZURE_WARBLER_SHOULDERS).orElse((byte) 0) : 0;
                state.setRenderData(SHOULDERS, mask);
            }
        });
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState player,
            float yRot, float xRot) {
        Byte mask = player.getRenderData(SHOULDERS);
        if (mask == null || mask == 0 || player.isInvisible) return;
        // Both shoulders share the same resting animation, but retain separate draw poses.
        var state = renderer.fillRenderState(bird, player, renderer.createRenderState(bird, player), player.partialTick);
        var camera = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;
        for (int side = 0; side < 2; side++) {
            if ((mask & (1 << side)) == 0) continue;
            pose.pushPose();
            // The supplied model's feet are at Y=0, unlike vanilla's Y=24 parrot.
            pose.translate(side == 0 ? 0.4F : -0.4F, player.isCrouching ? 0.2F : 0.0F, 0.0F);
            pose.scale(-1.0F, -1.0F, 1.0F);
            renderer.performRenderPass(state, pose, collector, camera);
            pose.popPose();
        }
    }

    private static final class PerchedBird implements GeoAnimatable {
        private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(new AnimationController<PerchedBird>("perch", 0, state -> state.setAndContinue(IDLE)));
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }
    }
}
