package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.entity.IserynValeEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

public final class IserynValeRenderer
        extends HumanoidMobRenderer<IserynValeEntity, AvatarRenderState, PlayerModel> {

    private static final Identifier TEXTURE = TheAurorian2.id("textures/entity/iseryn_vale.png");
    private static final PlayerSkin SKIN = new PlayerSkin(
            new ClientAsset.ResourceTexture(TheAurorian2.id("entity/iseryn_vale"), TEXTURE),
            null,
            null,
            PlayerModelType.SLIM,
            true);

    public IserynValeRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState state) {
        return state.skin.body().texturePath();
    }

    @Override
    public void extractRenderState(IserynValeEntity entity, AvatarRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.skin = SKIN;
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(IserynValeEntity entity, HumanoidArm arm) {
        return HumanoidModel.ArmPose.EMPTY;
    }
}
