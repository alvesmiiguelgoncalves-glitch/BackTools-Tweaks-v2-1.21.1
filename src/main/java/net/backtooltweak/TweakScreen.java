package net.backtooltweak;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Consumer;

/** Tela de config (Cloth Config): uma aba por mod, um grupo por item, com item 1 (costas) e item 2 (cintura). */
public final class TweakScreen {
    private TweakScreen() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Back Tool Tweak"))
                .setSavingRunnable(TweakConfig::save);
        ConfigEntryBuilder eb = builder.entryBuilder();

        for (Map.Entry<String, ItemTweak> e : TweakConfig.ITEMS.entrySet()) {
            Identifier id = Identifier.tryParse(e.getKey());
            if (id == null) continue;
            ItemTweak it = e.getValue();
            ConfigCategory category = builder.getOrCreateCategory(Text.literal(id.getNamespace()));

            SubCategoryBuilder sub = eb.startSubCategory(Text.literal(id.getPath()));
            sub.setExpanded(false);

            sub.add(eb.startTextDescription(Text.literal("-- Item 1 (nas costas) --")).build());
            addPose(eb, sub, it.item1);

            sub.add(eb.startTextDescription(Text.literal("-- Item 2 (na cintura) --")).build());
            Pose work2 = it.item2 != null ? it.item2 : it.item1.copy();
            sub.add(eb.startBooleanToggle(Text.literal("Usar config propria na cintura (se desligado, usa o item 1)"), it.item2 != null)
                    .setDefaultValue(false)
                    .setSaveConsumer(on -> it.item2 = on ? work2 : null)
                    .build());
            addPose(eb, sub, work2);

            SubCategoryListEntry entry = sub.build();
            category.addEntry(entry);
        }
        return builder.build();
    }

    private static void addPose(ConfigEntryBuilder eb, SubCategoryBuilder sub, Pose p) {
        sub.add(eb.startBooleanToggle(Text.literal("Visivel"), p.visible)
                .setDefaultValue(true).setSaveConsumer(v -> p.visible = v).build());
        sub.add(eb.startBooleanToggle(Text.literal("Espelhar lado"), p.mirror)
                .setDefaultValue(false).setSaveConsumer(v -> p.mirror = v).build());
        sub.add(f(eb, "x (lado, px)", p.x, -200, 200, v -> p.x = v));
        sub.add(f(eb, "y (altura, px)", p.y, -200, 200, v -> p.y = v));
        sub.add(f(eb, "z (profundidade, px)", p.z, -200, 200, v -> p.z = v));
        sub.add(f(eb, "rotX (graus)", p.rotX, -360, 360, v -> p.rotX = v));
        sub.add(f(eb, "rotY (graus)", p.rotY, -360, 360, v -> p.rotY = v));
        sub.add(f(eb, "rotZ (graus)", p.rotZ, -360, 360, v -> p.rotZ = v));
        sub.add(f(eb, "scale (0 = normal, 0.5 = 50% maior)", p.scale, -0.95f, 10, v -> p.scale = v));
    }

    private static AbstractConfigListEntry<Float> f(ConfigEntryBuilder eb, String label, float value,
                                                    float min, float max, Consumer<Float> save) {
        return eb.startFloatField(Text.literal(label), value)
                .setDefaultValue(0f).setMin(min).setMax(max).setSaveConsumer(save).build();
    }
}
