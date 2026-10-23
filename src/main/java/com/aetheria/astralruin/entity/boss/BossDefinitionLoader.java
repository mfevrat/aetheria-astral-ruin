package com.aetheria.astralruin.entity.boss;

import com.aetheria.astralruin.Aetheria;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/** Charge data/aetheria/bosses/valerius.json (rechargeable avec /reload). */
public class BossDefinitionLoader extends SimpleJsonResourceReloadListener {
    private static final ResourceLocation VALERIUS = Aetheria.id("valerius");

    public BossDefinitionLoader() {
        super(new Gson(), "bosses");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        JsonElement json = files.get(VALERIUS);
        if (json == null) {
            BossDefinition.setValerius(BossDefinition.DEFAULT);
            return;
        }
        try {
            BossDefinition.setValerius(BossDefinition.fromJson(json.getAsJsonObject()));
            Aetheria.LOGGER.info("Definition de Valerius chargee");
        } catch (Exception e) {
            Aetheria.LOGGER.error("valerius.json invalide, valeurs par defaut utilisees : {}", e.getMessage());
            BossDefinition.setValerius(BossDefinition.DEFAULT);
        }
    }
}
