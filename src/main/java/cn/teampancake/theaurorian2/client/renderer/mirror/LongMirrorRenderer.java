package cn.teampancake.theaurorian2.client.renderer.mirror;

import cn.teampancake.theaurorian2.common.block.PairedFurnitureBlock;
import cn.teampancake.theaurorian2.common.block.entity.LongMirrorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Discovery through normal section visibility, including mirrors crossing a section boundary. */
public final class LongMirrorRenderer implements BlockEntityRenderer<LongMirrorBlockEntity, LongMirrorRenderer.State> {
    public LongMirrorRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(LongMirrorBlockEntity be, State state, float partialTick,
            Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, camera, overlay);
        var block = be.getBlockState();
        var base = block.getValue(PairedFurnitureBlock.SECOND) ? be.getBlockPos().below() : be.getBlockPos();
        state.plane = new MirrorPlane(base, block.getValue(PairedFurnitureBlock.FACING));
    }
    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        // The level event selects a bounded number and submits their surfaces after offscreen rendering.
    }
    @Override public AABB getRenderBoundingBox(LongMirrorBlockEntity be) {
        var base = be.getBlockState().getValue(PairedFurnitureBlock.SECOND) ? be.getBlockPos().below() : be.getBlockPos();
        return new AABB(base).expandTowards(0, 1, 0);
    }
    @Override public int getViewDistance() { return 10; }
    public static final class State extends BlockEntityRenderState {
        public MirrorPlane plane;
    }
}
