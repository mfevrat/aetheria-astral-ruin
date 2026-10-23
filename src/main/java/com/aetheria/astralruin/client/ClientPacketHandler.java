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
        RuneCircleManager.add(new RuneCircleEmitter(msg.center(), msg.normal(), msg.preset(), msg.radius(), msg.duration()));
    }

    public static void shake(CameraShakeS2C msg) {
        CameraShakeHandler.shake(msg.power(), msg.ticks());
    }

    public static void syncMana(ManaSyncS2C msg) {
        ClientManaData.DATA.load(msg.tag());
    }

    private ClientPacketHandler() {}
}
