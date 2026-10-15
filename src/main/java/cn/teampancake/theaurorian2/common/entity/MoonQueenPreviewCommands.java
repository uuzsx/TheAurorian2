package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import java.util.Arrays;
import java.util.Comparator;

/** Animation inspection only. Does not use the supplied pack's combat scripts or damage. */
@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class MoonQueenPreviewCommands {
    private MoonQueenPreviewCommands() { }
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("aurorian_queen")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("animation").then(Commands.argument("name", StringArgumentType.word())
                        .suggests((context,builder) -> SharedSuggestionProvider.suggest(Arrays.stream(MoonQueenEntity.PREVIEWS).map(MoonQueenEntity.Preview::name),builder))
                        .executes(context -> play(context.getSource(),StringArgumentType.getString(context,"name")))))
                .then(Commands.literal("stop").executes(context -> {
                    var queen = nearest(context.getSource()); if (queen == null) return absent(context.getSource());
                    queen.stopPreview(); context.getSource().sendSuccess(() -> Component.translatable("message.theaurorian2.queen_animation_stopped"),false); return 1;
                }))
                .then(Commands.literal("list").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal(String.join(", ",Arrays.stream(MoonQueenEntity.PREVIEWS).map(MoonQueenEntity.Preview::name).toList())),false); return 1;
                })));
    }
    private static int play(CommandSourceStack source,String name) {
        var queen = nearest(source); if (queen == null) return absent(source);
        for (int i=0;i<MoonQueenEntity.PREVIEWS.length;i++) if (MoonQueenEntity.PREVIEWS[i].name().equals(name)) {
            queen.beginPreview(i); final int index=i;
            source.sendSuccess(() -> Component.translatable("message.theaurorian2.queen_animation",index+1,MoonQueenEntity.PREVIEWS.length,
                    Component.translatable("animation.theaurorian2.queen."+name)),false); return 1;
        }
        source.sendFailure(Component.translatable("message.theaurorian2.queen_animation_unknown",name)); return 0;
    }
    private static MoonQueenEntity nearest(CommandSourceStack source) {
        return source.getLevel().getEntitiesOfClass(MoonQueenEntity.class,new AABB(source.getPosition(),source.getPosition()).inflate(16),q -> q.isAlive())
                .stream().min(Comparator.comparingDouble(q -> q.position().distanceToSqr(source.getPosition()))).orElse(null);
    }
    private static int absent(CommandSourceStack source) { source.sendFailure(Component.translatable("message.theaurorian2.queen_animation_missing")); return 0; }
}
