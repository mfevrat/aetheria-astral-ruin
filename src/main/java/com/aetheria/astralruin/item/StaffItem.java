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
import com.aetheria.astralruin.spell.CastingManager;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Baton de l'Archi-Mage Celeste : clic droit = lance le sort selectionne (V pour changer). */
public class StaffItem extends Item implements GeoItem {
    private final String model;
    private final double spellPower;
    private final double maxMana;
    private final float costMultiplier;

    public StaffItem(Properties props) {
        this(props, "archmage_staff", 0.15, 100, 1.0F);
    }

    /** @param costMultiplier multiplicateur du cout en mana des sorts lances avec ce baton */
    public StaffItem(Properties props, String model, double spellPower, double maxMana, float costMultiplier) {
        super(props);
        this.model = model;
        this.spellPower = spellPower;
        this.maxMana = maxMana;
        this.costMultiplier = costMultiplier;
    }

    public float costMultiplier() {
        return costMultiplier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            boolean wasCasting = CastingManager.isCasting(sp);
            CastingManager.tryCastSelected(sp);
            if (!wasCasting && CastingManager.isCasting(sp)) {
                stack.hurtAndBreak(1, sp, p -> p.broadcastBreakEvent(hand));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
        b.putAll(super.getAttributeModifiers(slot, stack));
        b.put(ModAttributes.SPELL_POWER.get(), new AttributeModifier(ItemModifiers.STAFF_POWER, "Staff power",
                spellPower, AttributeModifier.Operation.ADDITION));
        b.put(ModAttributes.MAX_MANA.get(), new AttributeModifier(ItemModifiers.STAFF_MANA, "Staff mana",
                maxMana, AttributeModifier.Operation.ADDITION));
        return b.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.aetheria.archmage_staff.desc").withStyle(ChatFormatting.GRAY));
        if (costMultiplier < 1F) tooltip.add(Component.translatable("item.aetheria.awakened.cost",
                Math.round((1F - costMultiplier) * 100)).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(com.aetheria.astralruin.registry.ModItems.ASTRAL_INGOT.get());
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
