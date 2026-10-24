package com.aetheria.astralruin.spell;

import com.aetheria.astralruin.spell.impl.AstralBoltSpell;
import com.aetheria.astralruin.spell.impl.AstralJudgementSpell;
import com.aetheria.astralruin.spell.impl.RuneworkNovaSpell;
import com.aetheria.astralruin.spell.impl.RuneBarrierSpell;
import com.aetheria.astralruin.spell.impl.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpellRegistry {
    private static final Map<String, AbstractSpell> SPELLS = new LinkedHashMap<>();
    /** Ordre d'affichage et de selection. */
    public static final List<String> ORDER = List.of(
            "astral_bolt", "starfall", "comet_lance", "solar_halo", "astral_judgement",
            "runework_nova", "rune_barrier", "rune_mines", "glyph_prison", "gravity_sigil",
            "eclipse_step", "crescent_storm", "void_chains", "eclipse_requiem");

    static {
        register(new AstralBoltSpell());
        register(new RuneworkNovaSpell());
        register(new AstralJudgementSpell());
        register(new RuneBarrierSpell());
        register(new EclipseStepSpell());
        register(new StarfallSpell());
        register(new CometLanceSpell());
        register(new SolarHaloSpell());
        register(new RuneMinesSpell());
        register(new GlyphPrisonSpell());
        register(new GravitySigilSpell());
        register(new CrescentStormSpell());
        register(new VoidChainsSpell());
        register(new EclipseRequiemSpell());
    }

    private static void register(AbstractSpell spell) {
        SPELLS.put(spell.id(), spell);
    }

    public static AbstractSpell get(String id) {
        return SPELLS.get(id);
    }

    private SpellRegistry() {}
}
