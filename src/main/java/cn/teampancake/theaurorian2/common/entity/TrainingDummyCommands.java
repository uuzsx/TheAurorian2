package cn.teampancake.theaurorian2.common.entity;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import java.util.Comparator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class TrainingDummyCommands {

    private static final double SEARCH_RANGE = 16.0;

    private TrainingDummyCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("aurorian_dummy")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("armor")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 30.0))
                                .executes(context -> setArmor(
                                        context.getSource(), DoubleArgumentType.getDouble(context, "value")))))
                .then(Commands.literal("toughness")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 20.0))
                                .executes(context -> setToughness(
                                        context.getSource(), DoubleArgumentType.getDouble(context, "value")))))
                .then(Commands.literal("reset")
                        .executes(context -> reset(context.getSource())))
                .then(Commands.literal("info")
                        .executes(context -> info(context.getSource()))));
    }

    private static int setArmor(CommandSourceStack source, double value) {
        TrainingDummyEntity dummy = findNearest(source);
        if (dummy == null) {
            return notFound(source);
        }

        dummy.setTestArmor(value);
        source.sendSuccess(() -> Component.translatable(
                "commands.theaurorian2.training_dummy.armor", format(value)), false);
        return 1;
    }

    private static int setToughness(CommandSourceStack source, double value) {
        TrainingDummyEntity dummy = findNearest(source);
        if (dummy == null) {
            return notFound(source);
        }

        dummy.setTestArmorToughness(value);
        source.sendSuccess(() -> Component.translatable(
                "commands.theaurorian2.training_dummy.toughness", format(value)), false);
        return 1;
    }

    private static int reset(CommandSourceStack source) {
        TrainingDummyEntity dummy = findNearest(source);
        if (dummy == null) {
            return notFound(source);
        }

        dummy.resetTestDefenses();
        source.sendSuccess(() -> Component.translatable(
                "commands.theaurorian2.training_dummy.reset"), false);
        return 1;
    }

    private static int info(CommandSourceStack source) {
        TrainingDummyEntity dummy = findNearest(source);
        if (dummy == null) {
            return notFound(source);
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.theaurorian2.training_dummy.info",
                format(dummy.getTestArmor()),
                format(dummy.getTestArmorToughness())), false);
        return 1;
    }

    private static int notFound(CommandSourceStack source) {
        source.sendFailure(Component.translatable("commands.theaurorian2.training_dummy.not_found"));
        return 0;
    }

    private static TrainingDummyEntity findNearest(CommandSourceStack source) {
        Vec3 center = source.getPosition();
        AABB searchArea = AABB.ofSize(center, SEARCH_RANGE * 2.0, SEARCH_RANGE * 2.0, SEARCH_RANGE * 2.0);
        return source.getLevel().getEntitiesOfClass(TrainingDummyEntity.class, searchArea).stream()
                .min(Comparator.comparingDouble(dummy -> dummy.distanceToSqr(center)))
                .orElse(null);
    }

    private static String format(double value) {
        return value == Math.rint(value) ? Long.toString(Math.round(value)) : Double.toString(value);
    }
}
