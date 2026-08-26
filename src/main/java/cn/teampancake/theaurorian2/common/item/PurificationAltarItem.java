package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.block.PurificationAltarBasePartBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;

/** Places the altar at the base block's anchor, regardless of which part of its 3x3 model was hit. */
public final class PurificationAltarItem extends BlockItem {

    public PurificationAltarItem(Item.Properties properties) {
        super(ModBlocks.PURIFICATION_ALTAR.get(), properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var clickedState = context.getLevel().getBlockState(context.getClickedPos());
        if (clickedState.is(ModBlocks.PURIFICATION_ALTAR_BASE.get())
                || clickedState.is(ModBlocks.PURIFICATION_ALTAR_BASE_PART.get())) {
            var basePos = clickedState.is(ModBlocks.PURIFICATION_ALTAR_BASE.get())
                    ? context.getClickedPos()
                    : PurificationAltarBasePartBlock.masterPos(context.getClickedPos(), clickedState);
            BlockPlaceContext baseContext = new BlockPlaceContext(context);
            return place(BlockPlaceContext.at(baseContext, basePos, Direction.UP));
        }
        return super.useOn(context);
    }
}
