package com.aetheria.astralruin.skill;

import com.aetheria.astralruin.Aetheria;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

/** Charge data/<namespace>/skill_nodes/<id>.json. L'id du noeud est le nom du fichier. */
public class SkillNodeLoader extends SimpleJsonResourceReloadListener {
    public SkillNodeLoader() {
        super(new Gson(), "skill_nodes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, SkillTree.Node> out = new LinkedHashMap<>();
        files.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            String id = e.getKey().getPath();
            try {
                out.put(id, SkillTree.Node.fromJson(id, e.getValue().getAsJsonObject()));
            } catch (Exception ex) {
                Aetheria.LOGGER.error("Noeud runique invalide {} : {}", e.getKey(), ex.getMessage());
            }
        });
        if (out.isEmpty()) {
            Aetheria.LOGGER.warn("Aucun noeud runique trouve, utilisation de l'arbre par defaut.");
            return;
        }
        SkillTree.replace(out);
        Aetheria.LOGGER.info("Arbre runique charge : {} noeuds", out.size());
    }
}
