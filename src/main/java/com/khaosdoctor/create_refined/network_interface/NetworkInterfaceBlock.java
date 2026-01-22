package com.khaosdoctor.create_refined.network_interface;

import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;

public class NetworkInterfaceBlock extends Block implements IWrenchable {
  public static final String BLOCK_NAME = "network_interface";
  public static final DirectionProperty FACING = BlockStateProperties.FACING;

  public NetworkInterfaceBlock() {
    super(Properties.of()
        .destroyTime(Blocks.DIRT.defaultDestroyTime())
        .friction(Blocks.DIRT.getFriction())
        .lightLevel(value -> 1)
        .isRedstoneConductor((state, level, pos) -> false)
        .mapColor(MapColor.STONE));

    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    Direction facingPlayer = context.getNearestLookingDirection().getOpposite();
    // Block faces the player when placed
    return this.defaultBlockState().setValue(FACING, facingPlayer);
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
  }

  @Override
  public InteractionResult onWrenched(BlockState state, UseOnContext context) {
    // Ignore wrench interaction since the block cannot be rotated, but keep
    // the behavior of crouch + wrench to pick block
    return InteractionResult.PASS;
  }
}
