package cn.teampancake.theaurorian2.common.loot;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Replaces the base fishing catch before ordinary-priority loot modifiers add their rewards. */
public final class AurorianFishingLootModifier extends LootModifier {
    public static final MapCodec<AurorianFishingLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, AurorianFishingLootModifier::new));
    private static final ResourceKey<LootTable> FISHING = ResourceKey.create(
            Registries.LOOT_TABLE, TheAurorian2.id("gameplay/fishing"));

    public AurorianFishingLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!context.getLevel().dimension().equals(TheAurorian2.AURORIAN_LEVEL)
                || !(context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof FishingHook)) {
            return generatedLoot;
        }
        context.getResolver().get(FISHING).ifPresent(table -> {
            generatedLoot.clear();
            // Reuse the original hook, tool and luck. Raw generation avoids recursion and
            // lets downstream global modifiers process the final catch exactly once.
            table.value().getRandomItemsRaw(
                    context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        });
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
