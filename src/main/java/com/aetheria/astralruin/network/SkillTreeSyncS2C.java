package com.aetheria.astralruin.network;

import com.aetheria.astralruin.skill.SkillTree;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Envoie l'arbre runique du serveur (datapacks inclus) au client. */
public class SkillTreeSyncS2C {
    private final List<SkillTree.Node> nodes;

    public SkillTreeSyncS2C(List<SkillTree.Node> nodes) {
        this.nodes = nodes;
    }

    public SkillTreeSyncS2C(FriendlyByteBuf buf) {
        this(buf.readList(SkillTree.Node::read));
    }

    public static SkillTreeSyncS2C current() {
        return new SkillTreeSyncS2C(new ArrayList<>(SkillTree.nodes().values()));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(nodes, (b, n) -> n.write(b));
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        // Donnees pures : sans danger a appliquer des deux cotes (en solo, client et serveur partagent la meme JVM).
        Map<String, SkillTree.Node> map = new LinkedHashMap<>();
        for (SkillTree.Node n : nodes) map.put(n.id(), n);
        SkillTree.replace(map);
        ctx.setPacketHandled(true);
    }
}
