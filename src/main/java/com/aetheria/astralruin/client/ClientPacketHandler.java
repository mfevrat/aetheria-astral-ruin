package com.aetheria.astralruin.client;

import com.aetheria.astralruin.client.camera.CameraShakeHandler;
import com.aetheria.astralruin.client.particle.RuneCircleEmitter;
import com.aetheria.astralruin.client.particle.RuneCircleManager;
import com.aetheria.astralruin.network.CameraShakeS2C;
import com.aetheria.astralruin.network.ManaSyncS2C;
import com.aetheria.astralruin.network.SpawnRuneCircleS2C;

/** Seul point d'entree des paquets serveur -> client. Jamais reference sur serveur dedie. */
public final class ClientPacketHandler {
    public static void spawnCircle(SpawnRuneCircleS2C msg) {
        try {
            RuneCircleManager.add(new RuneCircleEmitter(msg.center(), msg.normal(), msg.preset(), msg.radius(), msg.duration()));
        } catch (Exception e) {
            ClientErrors.report("cercle " + msg.preset(), e);
        }
    }

    public static void spawnVfx(com.aetheria.astralruin.network.SpawnVfxS2C msg) {
        try {
            com.aetheria.astralruin.client.vfx.VfxManager.add(msg);
        } catch (Exception e) {
            ClientErrors.report("effet " + msg.type(), e);
        }
    }

    public static void cinematic(com.aetheria.astralruin.network.CinematicS2C msg) {
        try {
            com.aetheria.astralruin.client.cinematic.CinematicManager.start(msg.type(), msg.entityId());
        } catch (Exception e) {
            com.aetheria.astralruin.client.cinematic.CinematicManager.stop();
            ClientErrors.report("cinematique " + msg.type(), e);
        }
    }

    public static void shake(CameraShakeS2C msg) {
        CameraShakeHandler.shake(msg.power(), msg.ticks());
    }

    public static void syncMana(ManaSyncS2C msg) {
        ClientManaData.DATA.load(msg.tag());
    }

    private ClientPacketHandler() {}
}
