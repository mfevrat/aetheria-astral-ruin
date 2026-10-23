package com.aetheria.astralruin.item;

import com.aetheria.astralruin.client.render.geo.GeoClientExtensions;
import com.aetheria.astralruin.item.geo.GeoItemSupport;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import com.aetheria.astralruin.registry.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Fragment de Stele Celeste : clic droit = indique la direction de l'Observatoire le plus proche. */
public class SteleFragmentItem extends Item implements GeoItem {
    public SteleFragmentItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            player.getCooldowns().addCooldown(this, 100);
            BlockPos found = server.findNearestMapStructure(ModTags.OBSERVATORIES, player.blockPosition(), 100, false);
            if (found == null) {
                player.displayClientMessage(Component.translatable("message.aetheria.stele.none")
                        .withStyle(ChatFormatting.GRAY), false);
            } else {
                int dx = found.getX() - player.getBlockX();
                int dz = found.getZ() - player.getBlockZ();
                int dist = Mth.floor(Math.sqrt(dx * dx + dz * dz));
                player.displayClientMessage(Component.translatable("message.aetheria.stele.found",
                        dist, found.getX(), found.getZ()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1F, 0.8F);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.aetheria.stele_fragment.desc").withStyle(ChatFormatting.GRAY));
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
        consumer.accept(GeoClientExtensions.item(this, () -> "stele_fragment"));
    }

}
