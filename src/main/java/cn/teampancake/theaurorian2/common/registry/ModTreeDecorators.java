package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.worldgen.feature.AurorianTrunkVineDecorator;
import cn.teampancake.theaurorian2.common.worldgen.feature.TrunkMushroomDecorator;
import cn.teampancake.theaurorian2.common.worldgen.feature.StarblightTrunkFungusDecorator;
import cn.teampancake.theaurorian2.common.worldgen.feature.StarblightLeafSupportDecorator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTreeDecorators {

    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS =
            DeferredRegister.create(BuiltInRegistries.TREE_DECORATOR_TYPE, TheAurorian2.MOD_ID);

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<TrunkMushroomDecorator>>
            TRUNK_MUSHROOMS = TREE_DECORATORS.register(
                    "trunk_mushrooms", () -> new TreeDecoratorType<>(TrunkMushroomDecorator.CODEC));
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<AurorianTrunkVineDecorator>>
            TRUNK_VINES = TREE_DECORATORS.register(
                    "trunk_vines", () -> new TreeDecoratorType<>(AurorianTrunkVineDecorator.CODEC));

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<StarblightTrunkFungusDecorator>>
            STARBLIGHT_TRUNK_FUNGUS = TREE_DECORATORS.register(
                    "starblight_trunk_fungus", () -> new TreeDecoratorType<>(StarblightTrunkFungusDecorator.CODEC));

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<StarblightLeafSupportDecorator>>
            STARBLIGHT_LEAF_SUPPORT = TREE_DECORATORS.register(
                    "starblight_leaf_support", () -> new TreeDecoratorType<>(StarblightLeafSupportDecorator.CODEC));

    private ModTreeDecorators() {
    }

    public static void register(IEventBus modEventBus) {
        TREE_DECORATORS.register(modEventBus);
    }
}
