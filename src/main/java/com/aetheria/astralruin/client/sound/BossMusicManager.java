package com.aetheria.astralruin.client.sound;

import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.entity.boss.ValeriusEntity;
import com.aetheria.astralruin.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Musique de combat : demarre a 64 blocs d'un Valerius vivant, s'arrete en fondu apres sa mort. */
public final class BossMusicManager {
    private static final double RANGE = 64;
    @Nullable private static BossTheme playing;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            stopMusic(mc);
            return;
        }
        ValeriusEntity boss = null;
        if (AetheriaConfig.BOSS_MUSIC.get()) {
            List<ValeriusEntity> found = mc.level.getEntitiesOfClass(ValeriusEntity.class,
                    mc.player.getBoundingBox().inflate(RANGE), e -> !e.isRemoved() && e.isAlive());
            if (!found.isEmpty()) boss = found.get(0);
        }
        if (boss != null && (playing == null || playing.isStopped() || playing.fadingOut)) {
            mc.getMusicManager().stopPlaying();
            playing = new BossTheme(boss);
            mc.getSoundManager().play(playing);
        } else if (playing != null && boss != null) {
            playing.boss = boss;
        }
    }

    private static void stopMusic(Minecraft mc) {
        if (playing != null) {
            mc.getSoundManager().stop(playing);
            playing = null;
        }
    }

    private static final class BossTheme extends AbstractTickableSoundInstance {
        private ValeriusEntity boss;
        private boolean fadingOut;
        private int age;

        BossTheme(ValeriusEntity boss) {
            super(ModSounds.VALERIUS_THEME.get(), SoundSource.MUSIC, SoundInstance.createUnseededRandom());
            this.boss = boss;
            this.looping = true;
            this.delay = 0;
            this.relative = true;
            this.attenuation = Attenuation.NONE;
            this.volume = 0.01F;
        }

        @Override
        public void tick() {
            age++;
            Minecraft mc = Minecraft.getInstance();
            boolean bossGone = boss.isRemoved() || !boss.isAlive() || mc.player == null
                    || mc.player.distanceTo(boss) > RANGE + 16;
            if (bossGone) fadingOut = true;
            float target = fadingOut ? 0F : (boss.getPhase() == 2 ? 1.0F : 0.8F);
            volume = Mth.approach(volume, target, fadingOut ? 0.02F : 0.03F);
            // phase 2 : la musique monte legerement
            pitch = Mth.approach(pitch, boss.getPhase() == 2 ? 1.06F : 1.0F, 0.002F);
            if (fadingOut && volume <= 0.001F) this.stop();
        }
    }

    private BossMusicManager() {}
}
