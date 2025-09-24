package com.jdolphin.holoprojector.common.block;

import com.jdolphin.holoprojector.common.HoloProjector;
import com.jdolphin.holoprojector.common.packet.CBOpenGuiPacket;
import com.jdolphin.holoprojector.common.packet.HPPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.Color;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ALL")
public class HoloProjectorBlock extends HorizontalDirectionalBlock implements EntityBlock {
    protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);

    public HoloProjectorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public float getDestroyProgress(BlockState state, Player player, BlockGetter getter, BlockPos pos) {
        BlockEntity entity = getter.getBlockEntity(pos);
        if (entity instanceof HoloProjectorBlockEntity projector) {
            if (projector.isLocked() && !projector.getOwner().equals(player.getUUID())) {
                return 0.1f;
            }
        }
        return super.getDestroyProgress(state, player, getter, pos);
    }

    public RenderShape getRenderShape(BlockState p_51567_) {
        return RenderShape.MODEL;
    }

    public VoxelShape getCollisionShape(BlockState p_60572_, BlockGetter p_60573_, BlockPos p_60574_, CollisionContext p_60575_) {
        return AABB;
    }

    public VoxelShape getShape(BlockState p_49341_, BlockGetter p_49342_, BlockPos p_49343_, CollisionContext p_49344_) {
        return Block.box(1, 0, 1, 15, 32, 15);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HoloProjectorBlockEntity projector) {
                projector.setOwner(player.getUUID());
            }
        }
        super.setPlacedBy(level, pos, state, entity, stack);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HoloProjectorBlockEntity projector) {
                if (!projector.isLocked() || projector.getOwner().equals(player.getUUID())) {
                    if (player.getMainHandItem().getItem() instanceof DyeItem dyeItem) {
                        DyeColor dye = dyeItem.getDyeColor();
                        float[] rgb = dye.getTextureDiffuseColors();
                        projector.setColor(new Color(rgb[0], rgb[1], rgb[2]).getRGB());
                    } else {
                    String name = projector.getTargetPlayer() == null ? "" : projector.getTargetPlayer().getName();
                    HPPackets.sendTo(serverPlayer, new CBOpenGuiPacket(pos, name, projector.isLocked(), projector.isSlim(), projector.isSolid()));
                    return InteractionResult.SUCCESS;
                }
            }
            }
        }
        return super.use(state, level, pos, player, hand, result);
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return HoloProjector.PROJECTOR_ENTITY.get().create(pos, state);
    }
}
