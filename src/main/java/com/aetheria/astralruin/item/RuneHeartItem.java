package com.aetheria.astralruin.item;

import com.aetheria.astralruin.client.render.geo.GeoClientExtensions;
import com.aetheria.astralruin.item.geo.GeoItemSupport;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import com.aetheria.astralruin.mana.ManaCapability;
import com.aetheria.astralruin.mana.ManaEvents;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Coeur de Runes Grave : consomme pour +100 mana max et 3 Eclats Astraux. */
public class RuneHeartItem extends Item implements GeoItem {
    public RuneHeartItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) {
            ManaCapability.get(sp).ifPresent(data -> {
                data.addBonusMaxMana(100);
                data.addPoints(3);
                if (!sp.getAbilities().instabuild) stack.shrink(1);
                sp.displayClientMessage(Component.translatable("message.aetheria.rune_heart")
                        .withStyle(ChatFormatting.GOLD), true);
                Fx.burst((ServerLevel) level, sp.position().add(0, 1, 0), Fx.GOLD, 60, 0.4F);
                level.playSound(null, sp.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.7F, 1.3F);
                ManaEvents.sync(sp);
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.aetheria.rune_heart.desc").withStyle(ChatFormatting.GRAY));
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
        consumer.accept(GeoClientExtensions.item(this, () -> "rune_heart"));
    }

}
