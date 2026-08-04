package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import java.util.List;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModAccessoryItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheAurorian2.MOD_ID);

    public static final DeferredItem<Item> ARCANE_CANDLE = accessory("arcane_candle");
    public static final DeferredItem<Item> SEALED_ARTIFACT_ADVANCE = accessory("sealed_artifact_advance");
    public static final DeferredItem<Item> SEALED_ARTIFACT_CHOICE = accessory("sealed_artifact_choice");
    public static final DeferredItem<Item> SEALED_ARTIFACT_DESIRE = accessory("sealed_artifact_desire");
    public static final DeferredItem<Item> EIDOLON_STONE = accessory("eidolon_stone");
    public static final DeferredItem<Item> SHADOW_SIGHT = accessory("shadow_sight");
    public static final DeferredItem<Item> GLIMMERING_SCROLL = accessory("glimmering_scroll");
    public static final DeferredItem<Item> STAR_MOON_SAPLING = accessory("star_moon_sapling");
    public static final DeferredItem<Item> AURORIAN_BLESSING = accessory("aurorian_blessing");
    public static final DeferredItem<Item> POLAR_STAR_FRAGMENT = accessory("polar_star_fragment");
    public static final DeferredItem<Item> NECROMANTIC_SLATE = accessory("necromantic_slate");
    public static final DeferredItem<Item> ARCANE_DAGGER = accessory("arcane_dagger");
    public static final DeferredItem<Item> MYSTERIOUS_MUSHROOM = accessory("mysterious_mushroom");
    public static final DeferredItem<Item> GLOOMY_PAULDRONS = accessory("gloomy_pauldrons");

    public static final List<DeferredItem<Item>> ALL = List.of(
            ARCANE_CANDLE,
            SEALED_ARTIFACT_ADVANCE,
            SEALED_ARTIFACT_CHOICE,
            SEALED_ARTIFACT_DESIRE,
            EIDOLON_STONE,
            SHADOW_SIGHT,
            GLIMMERING_SCROLL,
            STAR_MOON_SAPLING,
            AURORIAN_BLESSING,
            POLAR_STAR_FRAGMENT,
            NECROMANTIC_SLATE,
            ARCANE_DAGGER,
            MYSTERIOUS_MUSHROOM,
            GLOOMY_PAULDRONS);

    private ModAccessoryItems() {
    }

    private static DeferredItem<Item> accessory(String name) {
        return ITEMS.registerItem(name, properties -> new Item(properties.stacksTo(1)));
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
