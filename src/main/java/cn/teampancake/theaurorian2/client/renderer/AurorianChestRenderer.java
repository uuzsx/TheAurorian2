package cn.teampancake.theaurorian2.client.renderer;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.AurorianChestBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class AurorianChestRenderer extends ChestRenderer<AurorianChestBlockEntity> {

    private static final SpriteId SINGLE = sprite("silent_wood");
    private static final SpriteId LEFT = sprite("silent_wood_left");
    private static final SpriteId RIGHT = sprite("silent_wood_right");

    public AurorianChestRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SpriteId getCustomSprite(AurorianChestBlockEntity chest, ChestRenderState state) {
        return switch (state.type) {
            case LEFT -> LEFT;
            case RIGHT -> RIGHT;
            case SINGLE -> SINGLE;
        };
    }

    private static SpriteId sprite(String name) {
        return new SpriteId(Sheets.CHEST_SHEET, TheAurorian2.id("entity/chest/" + name));
    }
}
