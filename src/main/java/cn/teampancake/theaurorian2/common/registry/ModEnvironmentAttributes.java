package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.AttributeRange;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEnvironmentAttributes {

    private static final DeferredRegister<EnvironmentAttribute<?>> ATTRIBUTES =
            DeferredRegister.create(Registries.ENVIRONMENT_ATTRIBUTE, TheAurorian2.MOD_ID);
    public static final DeferredHolder<EnvironmentAttribute<?>, EnvironmentAttribute<Float>>
            LOCAL_FOG_STRENGTH = ATTRIBUTES.register("local_fog_strength", () ->
                    EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(0.0F)
                            .spatiallyInterpolated().syncable().build());
    public static final DeferredHolder<EnvironmentAttribute<?>, EnvironmentAttribute<Integer>>
            LOCAL_FOG_COLOR = ATTRIBUTES.register("local_fog_color", () ->
                    EnvironmentAttribute.builder(AttributeTypes.RGB_COLOR).defaultValue(0xFF614578)
                            .spatiallyInterpolated().syncable().build());
    public static final DeferredHolder<EnvironmentAttribute<?>, EnvironmentAttribute<Float>>
            LOCAL_FOG_START_DISTANCE = ATTRIBUTES.register("local_fog_start_distance", () ->
                    EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(8.0F)
                            .spatiallyInterpolated().syncable().build());
    public static final DeferredHolder<EnvironmentAttribute<?>, EnvironmentAttribute<Float>>
            LOCAL_FOG_END_DISTANCE = ATTRIBUTES.register("local_fog_end_distance", () ->
                    EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(56.0F)
                            .valueRange(AttributeRange.NON_NEGATIVE_FLOAT)
                            .spatiallyInterpolated().syncable().build());

    private ModEnvironmentAttributes() {
    }

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
