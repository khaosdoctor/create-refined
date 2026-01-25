package com.khaosdoctor.refined_integrations.external_storage_interface;

import javax.annotation.Nullable;

import com.khaosdoctor.refined_integrations.RefinedIntegrations;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;

/**
 * The External Storage Interface block - bridges Create and Refined Storage.
 *
 * This is the Block class - it defines the physical properties and behavior of
 * the block itself.
 * The actual logic/data is in ExternalStorageInterfaceBlockEntity (the "brain"
 * of the
 * block).
 *
 * Implements two interfaces:
 * - IWrenchable: Makes it work with Create's wrench tool
 * - EntityBlock: Tells Minecraft this block has a BlockEntity attached
 *
 * Block Properties (stored in the blockstate):
 * - FACING: Which direction the block is facing (6 directions: up, down, north,
 * south, east, west)
 * - POWERED: Whether the block is part of an active RS network (true = on,
 * false = off)
 */
public class ExternalStorageInterfaceBlock extends Block implements EntityBlock {
  // The registry name for this block (used in game files, must match your block
  // JSON files)
  public static final String BLOCK_NAME = "external_storage_interface";

  // Block properties - these are stored in the "blockstate" and can change
  // without replacing the block
  public static final DirectionProperty FACING = BlockStateProperties.FACING; // Which way the block faces
  public static final BooleanProperty POWERED = BlockStateProperties.POWERED; // Is it active in the network?

  /**
   * Constructor - defines the physical properties of the block.
   *
   * Block properties explained:
   * - destroyTime: How long it takes to break (we use dirt's time as reference)
   * - friction: How slippery it is (we use dirt's friction)
   * - lightLevel: How much light it emits (1 = very dim, 15 = bright like
   * glowstone)
   * - isRedstoneConductor: Whether redstone signals can pass through (false =
   * signals can't pass)
   * - mapColor: Color shown on maps (STONE = gray)
   */
  public ExternalStorageInterfaceBlock() {
    super(Properties.of()
        .destroyTime(Blocks.DIRT.defaultDestroyTime()) // Same mining time as dirt
        .friction(Blocks.DIRT.getFriction()) // Same friction as dirt
        .lightLevel(value -> 1) // Emits a tiny bit of light
        .isRedstoneConductor((state, level, pos) -> false) // Doesn't conduct redstone
        .mapColor(MapColor.STONE)); // Shows as gray on maps

    // Register the default state - this is what the block looks like when first
    // placed
    this.registerDefaultState(
        this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH) // Default facing north
            .setValue(POWERED, false)); // Default to off (not powered)
  }

  /**
   * Creates the BlockEntity for this block.
   *
   * This is required by the EntityBlock interface. It tells Minecraft:
   * "When you place this block, also create a NetworkInterfaceBlockEntity to go
   * with it"
   *
   * @param pos   The position where the block is being placed
   * @param state The initial block state
   * @return A new NetworkInterfaceBlockEntity instance
   */
  @Override
  @Nullable
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new ExternalStorageInterfaceBlockEntity(pos, state);
  }

  /**
   * Provides the "ticker" - a function that runs every game tick (20 times per
   * second).
   *
   * Minecraft runs in ticks:
   * - 1 second = 20 ticks
   * - Each tick, the ticker runs
   * - This is how blocks do continuous work
   *
   * Why only server-side (level.isClientSide() check):
   * - Minecraft has two sides: client (what you see) and server (what's real)
   * - We only want logic to run on the server
   * - The server then tells the client what changed
   * - This prevents logic from running twice and keeps things in sync
   *
   * @param level           The world the block is in
   * @param state           The current block state
   * @param blockEntityType The type of block entity (for validation)
   * @return A ticker function that calls doWork() every tick, or null if no
   *         ticker needed
   */
  @Override
  @Nullable
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
      BlockEntityType<T> blockEntityType) {

    // Only tick on the server side, not the client
    // (client = what you see, server = what's real)
    if (level.isClientSide()) {
      return null;
    }

    // Only provide a ticker if this is actually our block entity type
    // (safety check to prevent crashes if something goes wrong)
    return blockEntityType == RefinedIntegrations.NETWORK_INTERFACE_BLOCK_ENTITY.get()
        ? (lvl, pos, st, blockEntity) -> {
          // Cast to our specific BlockEntity type and call doWork()
          if (blockEntity instanceof ExternalStorageInterfaceBlockEntity entity) {
            entity.doWork(); // This runs 20 times per second!
          }
        }
        : null;
  }

  /**
   * Registers which properties this block has in its blockstate.
   *
   * Blockstate explanation:
   * - A blockstate is like a configuration of the block
   * - Properties can change without replacing/breaking the block
   * - Examples: a door can be open/closed, a furnace can be on/off
   *
   * Our properties:
   * - FACING: Which direction the block faces (rotates without breaking)
   * - POWERED: Whether it's active in the RS network (changes when network status
   * changes)
   *
   * These properties are saved to the world and synced to clients.
   *
   * @param builder The builder to add properties to
   */
  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, POWERED);
  }

  /**
   * Determines the blockstate when a player places this block.
   *
   * This is called when a player right-clicks to place the block.
   * We use it to make the block face toward the player (like how furnaces work).
   *
   * How it works:
   * 1. Get the direction the player is looking
   * 2. Use the opposite direction (so the block faces the player)
   * 3. Start with POWERED = false (off until it joins a network)
   *
   * @param context Information about how/where the block is being placed
   * @return The initial blockstate for the newly placed block
   */
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    // Get the direction the player is looking, then reverse it
    // (so the block's front faces toward the player)
    Direction facingPlayer = context.getNearestLookingDirection().getOpposite();

    // Return a blockstate with:
    // - FACING set to face the player
    // - POWERED set to false (not active yet, will turn on when it joins a network)
    return this.defaultBlockState().setValue(FACING, facingPlayer).setValue(POWERED, false);
  }

  /**
   * Handles block rotation for structure blocks and commands.
   *
   * This is used by:
   * - Structure blocks (when rotating saved structures)
   * - Commands like /setblock with rotation
   * - Some mods that can rotate blocks
   *
   * We rotate the FACING property to match the rotation.
   *
   * @param state    The current blockstate
   * @param rotation How much to rotate (90°, 180°, 270°)
   * @return The rotated blockstate
   */
  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  /**
   * Handles block mirroring for structure blocks and commands.
   *
   * Similar to rotate(), but for mirroring (flipping) structures.
   * Used when pasting structures with mirror settings.
   *
   * We convert the mirror operation to a rotation and apply it.
   *
   * @param state  The current blockstate
   * @param mirror The mirror direction (left-right or front-back)
   * @return The mirrored blockstate
   */
  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
  }
}
