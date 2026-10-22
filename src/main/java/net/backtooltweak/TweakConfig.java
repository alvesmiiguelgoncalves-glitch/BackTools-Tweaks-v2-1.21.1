package net.backtooltweak;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Um arquivo .json por mod: config/backtooltweak/<mod>.json   (ex.: antarchy.json)
 * Formato:  { "antarchy:battle_axe": { "item1": {pose}, "item2": null } }
 * Arquivos de mods novos sao criados sozinhos quando voce adiciona um item daquele mod.
 */
public final class TweakConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private static final Type FILE_TYPE = new TypeToken<LinkedHashMap<String, ItemTweak>>() {}.getType();
    private static final Type OLD_TYPE = new TypeToken<LinkedHashMap<String, Pose>>() {}.getType();
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("backtooltweak");
    private static final Path OLD_FILE = FabricLoader.getInstance().getConfigDir().resolve("backtooltweak.json");

    /** Itens do Antarchy que costumam ir nas costas. Entradas zeradas nao mudam nada ate voce editar. */
    public static final String[] DEFAULT_ITEMS = {
            "battle_axe", "big_bertha", "giant_frying_pan",
            "royal_guardian_sword", "royal_guardian_shield", "royal_assailant_battleaxe", "royal_assailant_staff",
            "ultimate_sword", "ultimate_axe", "ultimate_pickaxe", "ultimate_bow", "ultimate_crossbow", "ultimate_mace",
            "blood_crystal_katana", "blood_gods_blade", "nightmare_sword",
            "scorpion_whip", "mantis_claw", "attitude_adjuster", "krakens_grasp", "eye_of_the_storm",
            "squidzooka", "rpo_launcher", "water_cannon", "shrink_ray", "growth_ray", "gravity_gun",
            "portal_gun", "portal_gun_atlas", "portal_gun_p_body", "portal_gun_craisin", "portal_gun_laynce"
    };

    public static Map<String, ItemTweak> ITEMS = new LinkedHashMap<>();
    /** namespaces que ja tinham arquivo (para gravar "{}" se ficarem vazios em vez de apagar). */
    private static final java.util.Set<String> KNOWN_NAMESPACES = new java.util.LinkedHashSet<>();

    private TweakConfig() {}

    public static void load() {
        ITEMS = new LinkedHashMap<>();
        KNOWN_NAMESPACES.clear();
        try {
            Files.createDirectories(DIR);
        } catch (IOException e) {
            BackToolTweakClient.LOGGER.error("Nao consegui criar {}", DIR, e);
        }

        List<Path> files = new ArrayList<>();
        try (Stream<Path> s = Files.list(DIR)) {
            s.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().forEach(files::add);
        } catch (IOException e) {
            BackToolTweakClient.LOGGER.error("Falha ao listar {}", DIR, e);
        }

        if (!files.isEmpty()) {
            for (Path f : files) loadFile(f);
        } else if (Files.exists(OLD_FILE)) {
            migrateOld();
        } else {
            for (String id : DEFAULT_ITEMS) ITEMS.put("antarchy:" + id, new ItemTweak());
            save();
        }
    }

    private static void loadFile(Path f) {
        String name = f.getFileName().toString();
        KNOWN_NAMESPACES.add(name.substring(0, name.length() - ".json".length()));
        try (Reader r = Files.newBufferedReader(f)) {
            Map<String, ItemTweak> loaded = GSON.fromJson(r, FILE_TYPE);
            if (loaded == null) return;
            for (Map.Entry<String, ItemTweak> e : loaded.entrySet()) {
                ItemTweak t = e.getValue();
                if (t == null) t = new ItemTweak();
                if (t.item1 == null) t.item1 = new Pose();
                ITEMS.put(e.getKey(), t);
            }
        } catch (Exception e) {
            BackToolTweakClient.LOGGER.error("Falha ao ler {}", f, e);
        }
    }

    /** Converte o backtooltweak.json antigo (1 pose por item) para os arquivos por mod. */
    private static void migrateOld() {
        try (Reader r = Files.newBufferedReader(OLD_FILE)) {
            Map<String, Pose> old = GSON.fromJson(r, OLD_TYPE);
            if (old != null) {
                for (Map.Entry<String, Pose> e : old.entrySet()) {
                    ITEMS.put(e.getKey(), new ItemTweak(e.getValue() == null ? new Pose() : e.getValue()));
                }
            }
            save();
            Files.move(OLD_FILE, OLD_FILE.resolveSibling("backtooltweak.old.json"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            BackToolTweakClient.LOGGER.error("Falha ao migrar {}", OLD_FILE, e);
        }
    }

    public static void save() {
        Map<String, Map<String, ItemTweak>> byMod = new LinkedHashMap<>();
        for (String ns : KNOWN_NAMESPACES) byMod.put(ns, new LinkedHashMap<>());
        for (Map.Entry<String, ItemTweak> e : ITEMS.entrySet()) {
            Identifier id = Identifier.tryParse(e.getKey());
            if (id == null) continue;
            byMod.computeIfAbsent(id.getNamespace(), k -> new LinkedHashMap<>()).put(e.getKey(), e.getValue());
        }
        try {
            Files.createDirectories(DIR);
            for (Map.Entry<String, Map<String, ItemTweak>> m : byMod.entrySet()) {
                KNOWN_NAMESPACES.add(m.getKey());
                try (Writer w = Files.newBufferedWriter(DIR.resolve(m.getKey() + ".json"))) {
                    GSON.toJson(m.getValue(), FILE_TYPE, w);
                }
            }
        } catch (IOException e) {
            BackToolTweakClient.LOGGER.error("Falha ao salvar em {}", DIR, e);
        }
    }

    public static ItemTweak get(Item item) {
        if (ITEMS.isEmpty()) return null;
        return ITEMS.get(Registries.ITEM.getId(item).toString());
    }

    public static ItemTweak getOrCreate(Identifier id) {
        return ITEMS.computeIfAbsent(id.toString(), k -> new ItemTweak());
    }

    public static List<Identifier> ids() {
        List<Identifier> out = new ArrayList<>();
        for (String k : ITEMS.keySet()) {
            Identifier id = Identifier.tryParse(k);
            if (id != null) out.add(id);
        }
        return out;
    }
}
