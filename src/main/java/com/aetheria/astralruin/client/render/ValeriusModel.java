package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.entity.boss.ValeriusEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * Charge automatiquement :
 * geo/entity/valerius.geo.json, animations/entity/valerius.animation.json, textures/entity/valerius.png
 */
public class ValeriusModel extends DefaultedEntityGeoModel<ValeriusEntity> {
    public ValeriusModel() {
        super(Aetheria.id("valerius"), true);
    }
}
