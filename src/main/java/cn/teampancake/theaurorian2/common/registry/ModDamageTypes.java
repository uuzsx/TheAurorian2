package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

import java.util.List;

public final class ModDamageTypes {

    public static final ResourceKey<DamageType> LACERATION = key("laceration");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT = key("corruption_settlement");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_2 = key("corruption_settlement_2");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_3 = key("corruption_settlement_3");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_4 = key("corruption_settlement_4");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_5 = key("corruption_settlement_5");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_6 = key("corruption_settlement_6");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_7 = key("corruption_settlement_7");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_8 = key("corruption_settlement_8");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_9 = key("corruption_settlement_9");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_10 = key("corruption_settlement_10");
    public static final ResourceKey<DamageType> CORRUPTION_SETTLEMENT_11 = key("corruption_settlement_11");

    private static final List<ResourceKey<DamageType>> CORRUPTION_SETTLEMENT_TYPES = List.of(
            CORRUPTION_SETTLEMENT,
            CORRUPTION_SETTLEMENT_2,
            CORRUPTION_SETTLEMENT_3,
            CORRUPTION_SETTLEMENT_4,
            CORRUPTION_SETTLEMENT_5,
            CORRUPTION_SETTLEMENT_6,
            CORRUPTION_SETTLEMENT_7,
            CORRUPTION_SETTLEMENT_8,
            CORRUPTION_SETTLEMENT_9,
            CORRUPTION_SETTLEMENT_10,
            CORRUPTION_SETTLEMENT_11
    );

    private ModDamageTypes() {
    }

    public static DamageSource source(ServerLevel level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(key));
    }

    public static DamageSource randomCorruptionSettlementSource(ServerLevel level) {
        ResourceKey<DamageType> key = CORRUPTION_SETTLEMENT_TYPES.get(
                level.getRandom().nextInt(CORRUPTION_SETTLEMENT_TYPES.size()));
        return source(level, key);
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, TheAurorian2.id(name));
    }
}
