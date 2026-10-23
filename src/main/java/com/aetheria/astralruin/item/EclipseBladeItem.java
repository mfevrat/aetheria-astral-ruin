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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Lame Runique d'Eclipse : chaque coup critique projette une lame de losanges (voir CommonEvents). */
public class EclipseBladeItem extends SwordItem implements GeoItem {
    private final String model;
    private final double spellPower;
    private final float slashDamage;
    private final int slashPierce;
    private final float manaLeech;

    public EclipseBladeItem(Tier tier, int damage, float speed, Properties props) {
        this(tier, damage, speed, props, "eclipse_blade", 0.05, 6F, 2, 0F);
    }

    public EclipseBladeItem(Tier tier, int damage, float speed, Properties props, String model,
                            double spellPower, float slashDamage, int slashPierce, float manaLeech) {
        super(tier, damage, speed, props);
        this.model = model;
        this.spellPower = spellPower;
        this.slashDamage = slashDamage;
        this.slashPierce = slashPierce;
        this.manaLeech = manaLeech;
    }

    public float slashDamage() { return slashDamage; }
    public int slashPierce() { return slashPierce; }
    public float manaLeech() { return manaLeech; }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
        b.putAll(super.getAttributeModifiers(slot, stack));
        b.put(ModAttributes.SPELL_POWER.get(), new AttributeModifier(ItemModifiers.BLADE_POWER, "Blade power",
                spellPower, AttributeModifier.Operation.ADDITION));
        return b.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.aetheria.eclipse_blade.desc").withStyle(ChatFormatting.GRAY));
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
        consumer.accept(GeoClientExtensions.item(this, () -> model));
    }

}
