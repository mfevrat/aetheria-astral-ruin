package com.aetheria.astralruin.spell;

import com.aetheria.astralruin.spell.impl.AstralBoltSpell;
import com.aetheria.astralruin.spell.impl.AstralJudgementSpell;
import com.aetheria.astralruin.spell.impl.RuneworkNovaSpell;
import com.aetheria.astralruin.spell.impl.RuneBarrierSpell;
import com.aetheria.astralruin.spell.impl.EclipseStepSpell;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpellRegistry {
    private static final Map<String, AbstractSpell> SPELLS = new LinkedHashMap<>();
    /** Ordre d'affichage et de selection. */
    public static final List<String> ORDER = List.of("astral_bolt", "runework_nova", "rune_barrier",
            "eclipse_step", "astral_judgement");

    static {
        register(new AstralBoltSpell());
        register(new RuneworkNovaSpell());
        register(new AstralJudgementSpell());
        register(new RuneBarrierSpell());
        register(new EclipseStepSpell());
    }

    private static void register(AbstractSpell spell) {
        SPELLS.put(spell.id(), spell);
    }

    public static AbstractSpell get(String id) {
        return SPELLS.get(id);
    }

    private SpellRegistry() {}
}
