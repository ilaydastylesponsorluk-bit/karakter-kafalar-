package com.karakter.kafalari;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/** Kafa bloğu: yüzü oyuncuya döner, boş elle sağ tıklayınca ses çıkarır. */
public class KafaBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<KafaBlock> CODEC = simpleCodec(KafaBlock::new);

    public KafaBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /** Her kafanın kendi sesi. Yeni kafa eklersen buraya da ekleyebilirsin. */
    protected SoundEvent ses() {
        String isim = BuiltInRegistries.BLOCK.getKey(this).getPath();
        return switch (isim) {
            case "koylu_kafa" -> SoundEvents.VILLAGER_AMBIENT;
            case "mavi_kiz_kafa" -> SoundEvents.CAT_AMBIENT;
            case "beyaz_gozlu_kafa" -> SoundEvents.ENDERMAN_STARE;
            case "salak_surat_kafa" -> SoundEvents.PLAYER_BURP;
            case "iskelet_surat_kafa" -> SoundEvents.SKELETON_AMBIENT;
            case "maske_kafa" -> SoundEvents.WITCH_CELEBRATE;
            default -> SoundEvents.VILLAGER_YES;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // Elinde blok varken normal şekilde blok koyabilesin diye sadece boş elle çalışır
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            float perde = 0.9f + level.random.nextFloat() * 0.3f;
            level.playSound(null, pos, ses(), SoundSource.BLOCKS, 1.0f, perde);
            server.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    0, level.random.nextDouble(), 0, 0, 1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
