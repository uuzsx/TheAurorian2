package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.crafting.AlchemyRecipe;
import cn.teampancake.theaurorian2.common.inventory.AlchemyTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class ModAlchemy {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TheAurorian2.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, TheAurorian2.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TheAurorian2.MOD_ID);
    public static final Supplier<MenuType<AlchemyTableMenu>> MENU = MENUS.register("alchemy_table",
            () -> new MenuType<>(AlchemyTableMenu::new, FeatureFlags.VANILLA_SET));
    public static final Supplier<RecipeType<AlchemyRecipe>> RECIPE_TYPE = TYPES.register("alchemy_table", () -> new RecipeType<>() {
        @Override public String toString() { return "theaurorian2:alchemy_table"; }
    });
    public static void register(IEventBus bus) {
        bus.addListener((net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent event) -> {
            event.register(AlchemyFormulaData.INGREDIENTS);
            event.register(AlchemyFormulaData.EFFECTS);
            event.register(AlchemyFormulaData.AMPLIFIERS);
        });
        bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) ->
                event.registerBlock(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,
                        (level, pos, state, entity, side) -> {
                            var block = ModBlocks.ALCHEMY_TABLE.get();
                            var primary = state.getValue(cn.teampancake.theaurorian2.common.block.PairedFurnitureBlock.SECOND)
                                    ? block.otherPos(state, pos) : pos;
                            return level.getBlockEntity(primary) instanceof cn.teampancake.theaurorian2.common.block.entity.AlchemyTableBlockEntity table
                                    ? new net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper(table, side) : null;
                        }, ModBlocks.ALCHEMY_TABLE.get()));
        SERIALIZERS.register("alchemy_table", () -> AlchemyRecipe.SERIALIZER);
        MENUS.register(bus); TYPES.register(bus); SERIALIZERS.register(bus);
    }
    private ModAlchemy() {}
}
