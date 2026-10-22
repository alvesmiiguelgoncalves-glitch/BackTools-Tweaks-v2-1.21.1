package net.backtooltweak;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BackToolTweakClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("backtooltweak");

    private static final SuggestionProvider<FabricClientCommandSource> ITEM_SUGGEST =
            (ctx, b) -> CommandSource.suggestIdentifiers(TweakConfig.ids(), b);
    private static final SuggestionProvider<FabricClientCommandSource> FIELD_SUGGEST =
            (ctx, b) -> CommandSource.suggestMatching(Arrays.asList(Pose.FIELDS), b);
    private static final SuggestionProvider<FabricClientCommandSource> SLOT_SUGGEST =
            (ctx, b) -> CommandSource.suggestMatching(List.of("item1", "item2"), b);

    @Override
    public void onInitializeClient() {
        TweakConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("backtool")
                        .then(literal("list").executes(ctx -> {
                            msg(ctx, "Itens configurados: " + TweakConfig.ITEMS.size());
                            TweakConfig.ITEMS.forEach((k, v) -> show(ctx, k, v));
                            return 1;
                        }))
                        .then(literal("reload").executes(ctx -> {
                            TweakConfig.load();
                            msg(ctx, "Config recarregada.");
                            return 1;
                        }))
                        .then(literal("add")
                                .executes(ctx -> {
                                    ItemStack held = ctx.getSource().getPlayer().getMainHandStack();
                                    if (held.isEmpty()) { msg(ctx, "Segure um item ou use /backtool add <item>."); return 0; }
                                    return add(ctx, Registries.ITEM.getId(held.getItem()));
                                })
                                .then(argument("item", IdentifierArgumentType.identifier())
                                        .executes(ctx -> add(ctx, ctx.getArgument("item", Identifier.class)))))
                        .then(literal("remove")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .executes(ctx -> {
                                            Identifier id = ctx.getArgument("item", Identifier.class);
                                            boolean ok = TweakConfig.ITEMS.remove(id.toString()) != null;
                                            TweakConfig.save();
                                            msg(ctx, ok ? "Removido: " + id : "Item nao estava na lista.");
                                            return ok ? 1 : 0;
                                        })))
                        .then(literal("show")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .executes(ctx -> {
                                            Identifier id = ctx.getArgument("item", Identifier.class);
                                            ItemTweak t = TweakConfig.ITEMS.get(id.toString());
                                            if (t == null) { msg(ctx, "Item nao esta na lista. Use /backtool add."); return 0; }
                                            show(ctx, id.toString(), t);
                                            return 1;
                                        })))
                        .then(literal("reset")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .executes(ctx -> {
                                            // sem slot: zera o item 1 e remove o item 2
                                            ItemTweak t = existing(ctx);
                                            if (t == null) return 0;
                                            t.item1.reset();
                                            t.item2 = null;
                                            TweakConfig.save();
                                            msg(ctx, "Zerado (item 1) e item 2 removido.");
                                            return 1;
                                        })
                                        .then(argument("slot", StringArgumentType.word()).suggests(SLOT_SUGGEST)
                                                .executes(ctx -> {
                                                    ItemTweak t = existing(ctx);
                                                    if (t == null) return 0;
                                                    String slot = StringArgumentType.getString(ctx, "slot");
                                                    if (slot.equals("item1")) t.item1.reset();
                                                    else if (slot.equals("item2")) t.item2 = null;
                                                    else { msg(ctx, "Slot invalido. Use item1 ou item2."); return 0; }
                                                    TweakConfig.save();
                                                    msg(ctx, "Zerado: " + slot);
                                                    return 1;
                                                }))))
                        .then(literal("item2")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .then(literal("on").executes(ctx -> {
                                            Identifier id = ctx.getArgument("item", Identifier.class);
                                            if (!Registries.ITEM.containsId(id)) { msg(ctx, "Item inexistente: " + id); return 0; }
                                            ItemTweak t = TweakConfig.getOrCreate(id);
                                            if (t.item2 == null) t.item2 = t.item1.copy();
                                            TweakConfig.save();
                                            msg(ctx, "Item 2 (cintura) ativado para " + id + " (copiado do item 1).");
                                            return 1;
                                        }))
                                        .then(literal("off").executes(ctx -> {
                                            ItemTweak t = existing(ctx);
                                            if (t == null) return 0;
                                            t.item2 = null;
                                            TweakConfig.save();
                                            msg(ctx, "Item 2 desativado: a cintura usa a pose do item 1.");
                                            return 1;
                                        }))))
                        .then(literal("set")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .then(argument("slot", StringArgumentType.word()).suggests(SLOT_SUGGEST)
                                                .then(argument("field", StringArgumentType.word()).suggests(FIELD_SUGGEST)
                                                        .then(argument("value", FloatArgumentType.floatArg())
                                                                .executes(ctx -> change(ctx, false)))))))
                        .then(literal("nudge")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .then(argument("slot", StringArgumentType.word()).suggests(SLOT_SUGGEST)
                                                .then(argument("field", StringArgumentType.word()).suggests(FIELD_SUGGEST)
                                                        .then(argument("delta", FloatArgumentType.floatArg())
                                                                .executes(ctx -> change(ctx, true)))))))
        ));
    }

    private static ItemTweak existing(CommandContext<FabricClientCommandSource> ctx) {
        Identifier id = ctx.getArgument("item", Identifier.class);
        ItemTweak t = TweakConfig.ITEMS.get(id.toString());
        if (t == null) msg(ctx, "Item nao esta na lista. Use /backtool add.");
        return t;
    }

    private static void show(CommandContext<FabricClientCommandSource> ctx, String id, ItemTweak t) {
        msg(ctx, id);
        msg(ctx, "  item 1 (costas): " + t.item1.summary());
        msg(ctx, t.item2 == null ? "  item 2 (cintura): usa o item 1" : "  item 2 (cintura): " + t.item2.summary());
    }

    private static int add(CommandContext<FabricClientCommandSource> ctx, Identifier id) {
        if (!Registries.ITEM.containsId(id)) { msg(ctx, "Item inexistente: " + id); return 0; }
        TweakConfig.getOrCreate(id);
        TweakConfig.save();
        msg(ctx, "Adicionado em config/backtooltweak/" + id.getNamespace() + ".json. Ajuste com /backtool set ou pelo Mod Menu.");
        return 1;
    }

    private static int change(CommandContext<FabricClientCommandSource> ctx, boolean nudge) {
        Identifier id = ctx.getArgument("item", Identifier.class);
        if (!Registries.ITEM.containsId(id)) { msg(ctx, "Item inexistente: " + id); return 0; }
        String slot = StringArgumentType.getString(ctx, "slot");
        boolean two = slot.equals("item2");
        if (!two && !slot.equals("item1")) { msg(ctx, "Slot invalido. Use item1 ou item2."); return 0; }
        String field = StringArgumentType.getString(ctx, "field");
        float v = FloatArgumentType.getFloat(ctx, nudge ? "delta" : "value");

        ItemTweak t = TweakConfig.getOrCreate(id);
        if (two && t.item2 == null) {
            t.item2 = t.item1.copy();
            msg(ctx, "Item 2 criado a partir do item 1.");
        }
        Pose p = two ? t.item2 : t.item1;
        if (nudge) v += p.get(field);
        if (!p.set(field, v)) { msg(ctx, "Campo invalido. Use: " + String.join(", ", Pose.FIELDS)); return 0; }
        TweakConfig.save();
        msg(ctx, id + " " + slot + ": " + p.summary());
        return 1;
    }

    private static void msg(CommandContext<FabricClientCommandSource> ctx, String s) {
        ctx.getSource().sendFeedback(Text.literal(s));
    }
}
