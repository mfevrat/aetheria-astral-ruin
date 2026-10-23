package com.aetheria.astralruin.client.render.geo;

import com.aetheria.astralruin.item.AstralArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;

import java.util.function.Supplier;

/** Fabriques d'extensions client : appelees uniquement depuis Item#initializeClient (cote client). */
public final class GeoClientExtensions {

    /**
     * Objet rendu par son modele 3D anime (inventaire, main, sol, cadre).
     * Le nom est lu paresseusement : Forge appelle initializeClient depuis le constructeur d'Item,
     * avant l'initialisation des champs de la sous-classe.
     */
    public static <T extends Item & GeoAnimatable> IClientItemExtensions item(T self, Supplier<String> model) {
        return new IClientItemExtensions() {
            private AetheriaItemRenderer<T> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new AetheriaItemRenderer<>(model.get());
                return renderer;
            }
        };
    }

    /** Piece d'armure : modele porte anime + icone 3D animee dans l'inventaire. */
    public static IClientItemExtensions armor(AstralArmorItem self, Supplier<String> iconModel) {
        return new IClientItemExtensions() {
            private AstralArmorRenderer armor;
            private AetheriaItemRenderer<AstralArmorItem> icon;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                                    EquipmentSlot slot, HumanoidModel<?> original) {
                if (armor == null) armor = new AstralArmorRenderer();
                armor.prepForRender(entity, stack, slot, original);
                return armor;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (icon == null) icon = new AetheriaItemRenderer<>(iconModel.get());
                return icon;
            }
        };
    }

    private GeoClientExtensions() {}
}
