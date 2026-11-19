package com.karakter.kafalari;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Tuzak kafa: oyuncu 3 blok yaklaşınca çığlık atar, duman patlatır ve ekranı karartır. */
public class KorkuBlock extends KafaBlock {
    public static final MapCodec<KorkuBlock> CODEC = simpleCodec(KorkuBlock::new);
    public static final BooleanProperty TETIK = BooleanProperty.create("tetik");

    public KorkuBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TETIK, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TETIK);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 10);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        Player oyuncu = level.getNearestPlayer(x, y, z, 3.0, false);
        boolean tetik = state.getValue(TETIK);

        if (oyuncu != null && !tetik) {
            level.playSound(null, pos, SoundEvents.GHAST_SCREAM, SoundSource.HOSTILE, 2.0f, 0.8f);
            level.playSound(null, pos, SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 2.0f, 0.6f);
            level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5, z, 3, 0.5, 0.5, 0.5, 0);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5, z, 30, 0.6, 0.6, 0.6, 0.05);
            oyuncu.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60));
            level.setBlock(pos, state.setValue(TETIK, true), Block.UPDATE_ALL);
        } else if (oyuncu == null && tetik) {
            // Oyuncu uzaklaşınca tuzak yeniden kurulur
            level.setBlock(pos, state.setValue(TETIK, false), Block.UPDATE_ALL);
        }
        level.scheduleTick(pos, this, 10);
    }
}
