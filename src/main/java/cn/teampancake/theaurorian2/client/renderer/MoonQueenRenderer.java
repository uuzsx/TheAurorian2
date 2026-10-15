package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.MoonQueenEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.google.common.reflect.TypeToken;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.AABB;
import java.util.WeakHashMap;

public final class MoonQueenRenderer extends SimpleGeoMobRenderer<MoonQueenEntity> {
    private static final DataTicket<MoonQueenPoseBlend> BLEND = DataTicket.create("moon_queen_pose_blend", new TypeToken<>() {});
    private static final DataTicket<Long> STAMP = DataTicket.create("moon_queen_pose_stamp", new TypeToken<>() {});
    private static final DataTicket<Double> DURATION = DataTicket.create("moon_queen_pose_duration", new TypeToken<>() {});
    private static final DataTicket<Boolean> PREVIEW = DataTicket.create("moon_queen_pose_preview", new TypeToken<>() {});
    private final WeakHashMap<MoonQueenEntity, MoonQueenPoseBlend> blends = new WeakHashMap<>();

    public MoonQueenRenderer(EntityRendererProvider.Context context) {
        super(context, TheAurorian2.id("moon_queen"), .3F);
    }

    @Override public void extractRenderState(MoonQueenEntity entity, LivingEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.scale *= MoonQueenEntity.MODEL_SCALE;
    }

    @Override public void addRenderData(MoonQueenEntity entity, Void relatedObject, LivingEntityRenderState state, float partialTick) {
        GeoRenderState geo = (GeoRenderState)(Object)state;
        geo.addGeckolibData(BLEND, blends.computeIfAbsent(entity, ignored -> new MoonQueenPoseBlend()));
        geo.addGeckolibData(STAMP, entity.animationStateStamp());
        geo.addGeckolibData(PREVIEW, entity.previewIndex() >= 0);
        geo.addGeckolibData(DURATION, switch (entity.motion()) {
            case START -> .12; case STOP -> .18; case WALK, IDLE -> .08;
        });
    }

    @Override @SuppressWarnings({"rawtypes", "unchecked"})
    public void adjustModelBonesForRender(RenderPassInfo pass, BoneSnapshots snapshots) {
        MoonQueenPoseBlend blend = (MoonQueenPoseBlend)pass.getGeckolibData(BLEND);
        blend.apply(pass.model(), snapshots, (Long)pass.getGeckolibData(STAMP),
                pass.renderState().getAnimatableAge() / 20, (Double)pass.getGeckolibData(DURATION),
                (Boolean)pass.getGeckolibData(PREVIEW));
    }

    @Override protected AABB getBoundingBoxForCulling(MoonQueenEntity entity) {
        // The sword and long hair extend beyond the physical body box.
        double margin = entity.previewIndex() >= 0 ? 5 : 2;
        return super.getBoundingBoxForCulling(entity).inflate(margin, margin, margin);
    }
}
