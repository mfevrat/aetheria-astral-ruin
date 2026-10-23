package com.aetheria.astralruin.item;

import com.aetheria.astralruin.client.render.geo.GeoClientExtensions;
import com.aetheria.astralruin.item.geo.GeoItemSupport;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import com.aetheria.astralruin.registry.ModAttributes;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AstralArmorItem extends ArmorItem implements GeoItem {
    public AstralArmorItem(Type type, Properties props) {
        super(AstralArmorMaterial.ASTRAL, type, props);
    }

    /** Modele 3D de l'icone : astral_helmet, astral_chestplate, astral_leggings ou astral_boots. */
    public String iconModel() {
        return "astral_" + getType().getName();
    }

    public static boolean hasFullSet(LivingEntity entity) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof AstralArmorItem)) return false;
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != getEquipmentSlot()) return super.getAttributeModifiers(slot, stack);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
        b.putAll(super.getAttributeModifiers(slot, stack));
        var id = ItemModifiers.ARMOR[slot.getIndex()];
        switch (getType()) {
            case HELMET -> b.put(ModAttributes.MAX_MANA.get(),
                    new AttributeModifier(id, "Astral helmet", 50, AttributeModifier.Operation.ADDITION));
            case CHESTPLATE -> b.put(ModAttributes.SPELL_POWER.get(),
                    new AttributeModifier(id, "Astral chestplate", 0.1, AttributeModifier.Operation.ADDITION));
            case LEGGINGS -> b.put(ModAttributes.MANA_REGEN.get(),
                    new AttributeModifier(id, "Astral leggings", 0.5, AttributeModifier.Operation.ADDITION));
            case BOOTS -> b.put(ModAttributes.BACKLASH_RESISTANCE.get(),
                    new AttributeModifier(id, "Astral boots", 0.1, AttributeModifier.Operation.ADDITION));
        }
        return b.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.aetheria.astral_armor.set").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.aetheria.astral_armor.set.desc").withStyle(ChatFormatting.GRAY));
    }

    // ---------- Rendu 3D anime (GeckoLib) ----------
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        GeoItemSupport.idle(this, controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(GeoClientExtensions.armor(this, this::iconModel));
    }

}
