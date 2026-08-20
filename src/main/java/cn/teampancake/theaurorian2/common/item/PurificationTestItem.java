package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.common.world.MoonShieldSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class PurificationTestItem extends Item {

    public PurificationTestItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            boolean purified = MoonShieldSystem.purify(serverPlayer);
            serverPlayer.sendSystemMessage(Component.translatable(purified
                    ? "message.theaurorian2.purification.completed"
                    : "message.theaurorian2.purification.already_completed"));
        }
        return InteractionResult.SUCCESS;
    }
}
