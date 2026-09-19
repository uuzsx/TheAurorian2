package cn.teampancake.theaurorian2.common.crafting;

import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;

/** Pure preparation: no input mutation, consumption happens only after checking the complete output. */
public final class AlchemyProcessing {
    public static final int MAX_DURATION = 24000;
    private static final List<List<Holder<MobEffect>>> CONFLICTS = List.of(
            List.of(MobEffects.SPEED, MobEffects.SLOWNESS), List.of(MobEffects.HASTE, MobEffects.MINING_FATIGUE),
            List.of(MobEffects.STRENGTH, MobEffects.WEAKNESS), List.of(MobEffects.REGENERATION, MobEffects.POISON),
            List.of(MobEffects.NIGHT_VISION, MobEffects.BLINDNESS), List.of(MobEffects.SATURATION, MobEffects.HUNGER),
            List.of(MobEffects.LUCK, MobEffects.UNLUCK), List.of(MobEffects.INSTANT_HEALTH, MobEffects.INSTANT_DAMAGE));

    public record Plan(ItemStack output, int[] consumption, boolean bottle, int time) {}

    public static Plan prepare(List<ItemStack> inputs) {
        ItemStack material = inputs.get(3);
        if (!material.is(Items.POTION)) return null;
        PotionContents contents = material.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (!contents.hasEffects()) return null;
        int blue = -1, moon = -1, potion = -1, food = -1, occupied = 0;
        for (int i = 0; i < 3; i++) {
            ItemStack stack = inputs.get(i);
            if (stack.isEmpty()) continue;
            occupied++;
            if (stack.is(ModLegacyItems.CERULEAN_NUGGET.get())) blue = i;
            else if (stack.is(ModLegacyItems.MOONSTONE_NUGGET.get())) moon = i;
            else if (stack.is(Items.POTION)) potion = i;
            else if (stack.has(DataComponents.FOOD) && stack.has(DataComponents.CONSUMABLE)) food = i;
        }
        int[] consumption = new int[4]; consumption[3] = 1;
        if (blue >= 0 && moon >= 0 && potion >= 0) {
            PotionContents base = inputs.get(potion).getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            List<MobEffectInstance> effects = merge(base.getAllEffects(), contents.getAllEffects());
            ItemStack output = new ItemStack(Items.POTION);
            output.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects, Optional.empty()));
            output.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.translatable(
                    "item.theaurorian2.mixed_potion").withStyle(style -> style.withItalic(false)));
            Arrays.fill(consumption, 1);
            return new Plan(output, consumption, true, 140);
        }
        if (occupied != 1 || food < 0) return null;
        ItemStack output = inputs.get(food).copyWithCount(1);
        List<MobEffectInstance> effects = merge(output.getOrDefault(DataComponents.POTION_CONTENTS,
                PotionContents.EMPTY).getAllEffects(), contents.getAllEffects());
        // PotionContents is both a tooltip provider and a vanilla consumable listener.
        // It applies these effects itself; do not also append ApplyStatusEffectsConsumeEffect.
        output.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects, Optional.empty()));
        consumption[food] = 1;
        return new Plan(output, consumption, true, 140);
    }

    public static List<MobEffectInstance> merge(Iterable<MobEffectInstance> first, Iterable<MobEffectInstance> second) {
        Map<Holder<MobEffect>, MobEffectInstance> merged = new LinkedHashMap<>();
        for (Iterable<MobEffectInstance> source : List.of(first, second)) for (MobEffectInstance effect : source) {
            MobEffectInstance previous = merged.get(effect.getEffect());
            int ticks = duration(effect);
            int amplifier = effect.getAmplifier();
            if (previous != null) { ticks = Math.min(MAX_DURATION, ticks + duration(previous)); amplifier = Math.max(amplifier, previous.getAmplifier()); }
            merged.put(effect.getEffect(), new MobEffectInstance(effect.getEffect(), ticks, amplifier));
        }
        for (List<Holder<MobEffect>> pair : CONFLICTS) {
            MobEffectInstance a = merged.get(pair.get(0)), b = merged.get(pair.get(1));
            if (a == null || b == null) continue;
            long difference = (long) (a.getAmplifier() + 1) * duration(a) - (long) (b.getAmplifier() + 1) * duration(b);
            merged.remove(pair.get(0)); merged.remove(pair.get(1));
            if (difference == 0) continue;
            MobEffectInstance winner = difference > 0 ? a : b;
            int remaining = (int) (Math.abs(difference) / (winner.getAmplifier() + 1));
            if (remaining > 0) merged.put(winner.getEffect(), new MobEffectInstance(winner.getEffect(), remaining, winner.getAmplifier()));
        }
        return List.copyOf(merged.values());
    }
    private static int duration(MobEffectInstance effect) {
        return effect.isInfiniteDuration() ? MAX_DURATION : Math.clamp(effect.getDuration(), 1, MAX_DURATION);
    }
    private AlchemyProcessing() {}
}
