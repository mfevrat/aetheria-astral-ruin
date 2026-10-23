package com.aetheria.astralruin.block;

import com.aetheria.astralruin.entity.boss.ValeriusEntity;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModItems;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.aetheria.astralruin.block.entity.AstralAltarBlockEntity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Autel Astral : utiliser un Sceau Astral dessus invoque Valerius. */
public class AstralAltarBlock extends Block implements EntityBlock {
    public AstralAltarBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstralAltarBlockEntity(pos, state);
    }

    /** Le modele est dessine par le renderer GeckoLib de l'entite de bloc. */
    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.ASTRAL_SIGIL.get())) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.aetheria.altar.hint")
                        .withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (level instanceof ServerLevel server) {
            boolean alreadyHere = !server.getEntitiesOfClass(ValeriusEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(64)).isEmpty();
            if (alreadyHere) {
                player.displayClientMessage(Component.translatable("message.aetheria.altar.busy")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.CONSUME;
            }
            ValeriusEntity boss = ModEntities.VALERIUS.get().create(server);
            if (boss == null) return InteractionResult.FAIL;
            BlockPos spawn = pos.north(5);
            boss.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 180F, 0F);
            boss.setAnchor(pos);
            
            boss.finalizeSpawn(server, server.getCurrentDifficultyAt(spawn), MobSpawnType.EVENT, null, null);
            server.addFreshEntity(boss);
            if (!player.getAbilities().instabuild) stack.shrink(1);

            Vec3 c = Vec3.atBottomCenterOf(pos.above());
            Fx.groundCircle(server, c, RuneCirclePreset.TRANSITION, 8F, 60);
            Fx.burst(server, Vec3.atCenterOf(spawn), Fx.VIOLET, 80, 0.5F);
            Fx.shake(server, c, 0.6F, 30, 48);
            server.playSound(null, pos, ModSounds.VALERIUS_TRANSITION.get(), SoundSource.HOSTILE, 3F, 1.2F);
            player.displayClientMessage(Component.translatable("message.aetheria.altar.summon")
                    .withStyle(ChatFormatting.DARK_PURPLE), false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
