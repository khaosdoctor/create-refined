package com.khaosdoctor.create_refined.network_interface;

import java.util.Set;

import com.khaosdoctor.create_refined.CreateRefined;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;

public class NetworkInterfaceBlock extends Block implements IWrenchable {
  public static final String BLOCK_NAME = "network_interface";
  public static final DirectionProperty FACING = BlockStateProperties.FACING;
  public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
  private static final Set<String> ALLOWED_NEIGHBORS = Set.of(
      "refinedstorage:controller",
      "refinedstorage:creative_controller",
      "refinedstorage:grid",
      "refinedstorage:crafting_grid",
      "refinedstorage:pattern_grid",
      "refinedstorage:disk_drive",
      "refinedstorage:fluid_grid",
      "refinedstorage:portable_grid",
      "refinedstorage:creative_portable_grid",
      "refinedstorage:autocrafter",
      "refinedstorage:autocrafter_manager",
      "refinedstorage:autocrafting_monitor",
      "refinedstorage:relay",
      "refinedstorage:security_manager",
      "refinedstorage:cable",
      "refinedstorage:storage_monitor",
      "refinedstorage:network_transmitter",
      "refinedstorage:network_receiver",
      "refinedstorage:disk_interface",

      CreateRefined.NETWORK_INTERFACE.getId().toString()); // Allow other Network Interfaces as neighbors

  public NetworkInterfaceBlock() {
    super(Properties.of()
        .destroyTime(Blocks.DIRT.defaultDestroyTime())
        .friction(Blocks.DIRT.getFriction())
        .lightLevel(value -> 1)
        .isRedstoneConductor((state, level, pos) -> false)
        .mapColor(MapColor.STONE));

    this.registerDefaultState(
        this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, false));
  }

  /**
   * Checks whether the blocks around the Network Interface are from Refined
   * Storage
   * TODO: This should be changed because it's not all blocks from RS that will be
   * used
   * to transmit power signals.
   *
   * @param level World level
   * @param pos   Position of the Network Interface
   * @return True if there is at least one RS block neighbor, false otherwise
   */
  private boolean shouldBePowered(Level level, BlockPos pos) {
    for (Direction direction : Direction.values()) {
      BlockPos neighborPos = pos.relative(direction);
      BlockState neighborState = level.getBlockState(neighborPos);

      ResourceLocation id = BuiltInRegistries.BLOCK.getKey(neighborState.getBlock());

      if (id != null && ALLOWED_NEIGHBORS.contains(id.toString())) {
        CreateRefined.LOGGER
            .info("Found RS neighbor at " + neighborPos + ": " + id + " this interface should be powered");
        return true;
      }

      CreateRefined.LOGGER.info("Neighbor at " + neighborPos + ": " + id + " is not an allowed RS block");
    }
    return false;
  }

  @Override
  protected void neighborChanged(
      BlockState state,
      Level level,
      BlockPos pos,
      Block neighborBlock,
      BlockPos neighborPos,
      boolean movedByPiston) {
    CreateRefined.LOGGER.info("Neighbor changed for Network Interface at " + pos + ": " + neighborBlock + " at "
        + neighborPos + " the current powered state is " + state.getValue(POWERED));

    if (level.isClientSide()) {
      return;
    }

    boolean shouldBePowered = shouldBePowered(level, pos);
    CreateRefined.LOGGER
        .info("Network Interface at " + pos + " should be powered: " + shouldBePowered);
    if (state.getValue(POWERED) != shouldBePowered) {
      level.setBlock(pos, state.setValue(POWERED, shouldBePowered), Block.UPDATE_ALL);
    }
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, POWERED);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    Direction facingPlayer = context.getNearestLookingDirection().getOpposite();
    boolean initialPoweredState = shouldBePowered(context.getLevel(), context.getClickedPos());
    CreateRefined.LOGGER
        .info("Placing Network Interface at " + context.getClickedPos() + " facing " + facingPlayer
            + " with initial powered state " + initialPoweredState);
    // Block faces the player when placed
    return this.defaultBlockState().setValue(FACING, facingPlayer).setValue(POWERED, initialPoweredState);
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
