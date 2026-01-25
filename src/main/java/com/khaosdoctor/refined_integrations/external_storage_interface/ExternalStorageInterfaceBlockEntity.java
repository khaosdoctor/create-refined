package com.khaosdoctor.refined_integrations.external_storage_interface;

import com.khaosdoctor.refined_integrations.RefinedIntegrations;
import com.khaosdoctor.refined_integrations.external_storage_interface.rs_integration.ExternalStorageInterfaceNetworkNode;
import com.khaosdoctor.refined_integrations.external_storage_interface.rs_integration.NetworkItemHandler;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.network.SimpleConnectionStrategy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * BlockEntity for the External Storage Interface block.
 *
 * This is the "brain" behind the External Storage Interface block. In Minecraft
 * modding:
 * - A Block is just the visual/physical thing you place in the world
 * - A BlockEntity is the data and logic attached to that block
 *
 * We extend AbstractBaseNetworkNodeContainerBlockEntity which is a base class
 * from
 * Refined Storage that gives us automatic network integration. This base class
 * handles:
 * - Connecting to nearby RS cables/blocks
 * - Joining and leaving the RS network automatically
 * - Tracking whether we're active (powered) or not
 * - Energy management
 *
 * The <ExternalStorageInterfaceNetworkNode> part means this BlockEntity
 * contains/manages
 * an ExternalStorageInterfaceNetworkNode (our representation in the RS network
 * graph).
 */
public class ExternalStorageInterfaceBlockEntity
    extends AbstractBaseNetworkNodeContainerBlockEntity<ExternalStorageInterfaceNetworkNode> {

  // Adds the item handler for RS integration so we can store items in the network
  private NetworkItemHandler itemHandler;

  /**
   * Constructor - called when the block is placed in the world.
   *
   * @param pos   The position in the world where this block is located
   * @param state The block state (includes properties like FACING and POWERED)
   */
  public ExternalStorageInterfaceBlockEntity(BlockPos pos, BlockState state) {
    // Call the parent class constructor with:
    // 1. Our block entity type (so Minecraft knows what type this is)
    // 2. The position in the world
    // 3. The block state
    // 4. A new ExternalStorageInterfaceNetworkNode (our node in the RS network
    // graph)
    super(
        RefinedIntegrations.NETWORK_INTERFACE_BLOCK_ENTITY.get(),
        pos,
        state,
        new ExternalStorageInterfaceNetworkNode());
  }

  public IItemHandler getItemHandler() {
    if (itemHandler == null) {
      // Create the item handler
      itemHandler = new NetworkItemHandler(mainNetworkNode);
    }
    return itemHandler;
  }

  /**
   * Called when the BlockEntity is added to the world and ready to use.
   *
   * Lifecycle explanation:
   * 1. Constructor is called when the block is first created
   * 2. clearRemoved() is called when it's actually ready to join the world
   * 3. This is where we initialize connections to the RS network
   *
   * Think of it like: constructor = birth, clearRemoved = coming online
   */
  @Override
  public void clearRemoved() {
    // Call parent's clearRemoved - this joins us to the RS network
    // The parent class handles all neighbor notifications automatically
    super.clearRemoved();
  }

  /**
   * Called when the BlockEntity is being removed from the world.
   *
   * This happens when:
   * - The block is broken by a player
   * - The chunk is unloaded
   * - The block is replaced by another block
   *
   * This is our cleanup method - we need to disconnect from the RS network.
   */
  @Override
  public void setRemoved() {
    // Call parent's setRemoved - this removes us from the RS network
    // and tells connected cables/nodes that we're gone
    super.setRemoved();
  }

  /**
   * Creates the "container" that wraps our network node.
   *
   * RS Network Architecture Explained:
   * - NetworkNode = the data/logic (like NetworkInterfaceNetworkNode)
   * - NetworkNodeContainer = connects that data to a physical block in the world
   * - This method tells RS how our block should behave in the network
   *
   * Think of it like:
   * - NetworkNode = your brain (the logic)
   * - NetworkNodeContainer = your body (the physical presence)
   * - ConnectionStrategy = which sides of your body can shake hands with others
   *
   * @param node The NetworkInterfaceNetworkNode that this container will wrap
   * @return The configured container that manages our network presence
   */
  @Override
  protected InWorldNetworkNodeContainer createMainContainer(ExternalStorageInterfaceNetworkNode node) {
    // Build a container using the RS API
    InWorldNetworkNodeContainer container = RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, node)
        .name("main") // Name of this container (you can have multiple containers per BlockEntity, we
                      // only have one)
        // SimpleConnectionStrategy means we can connect from ALL 6 sides (north, south,
        // east, west, up, down)
        // Alternative: ColoredConnectionStrategy excludes the facing direction (useful
        // if you want one side free)
        .connectionStrategy(new SimpleConnectionStrategy(getBlockPos()))
        .build();
    return container;
  }

  /**
   * Called when our "active" status changes in the RS network.
   *
   * What "active" means in RS:
   * - We're connected to a network
   * - The network has a controller
   * - The network has enough energy
   * - All connections are valid
   *
   * We use this to update our block's POWERED property so players can see
   * whether this block is actually part of a working network or not.
   *
   * @param newActive true if we just became active, false if we became inactive
   */
  @SuppressWarnings("null")
  @Override
  protected void activenessChanged(boolean newActive) {
    // Always call parent first - important for RS to track state properly
    super.activenessChanged(newActive);

    // Only update block state on the server side
    // In Minecraft, there's a client (what you see) and server (what's real)
    // We only want to change the "real" state on the server, then it syncs to
    // client
    if (level != null && !level.isClientSide()) {
      // Get the current block state (this includes properties like FACING and
      // POWERED)
      BlockState currentState = getBlockState();

      // Only update if the POWERED property is different from our new active state
      // (avoid unnecessary updates)
      if (currentState.getValue(ExternalStorageInterfaceBlock.POWERED) != newActive) {
        // Create a new state with POWERED set to match our network active state
        BlockState newState = currentState.setValue(ExternalStorageInterfaceBlock.POWERED, newActive);
        // Update the block in the world
        // Block.UPDATE_ALL means: update clients, update neighbors, etc.
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
      }
    }
  }

  /**
   * Called every game tick (20 times per second) to do work.
   *
   * This is the "ticker" - it runs continuously while the block exists in the
   * world.
   * In our ExternalStorageInterfaceBlock.java, we set up getTicker() to call this
   * method.
   *
   * What happens each tick:
   * 1. super.doWork() - runs the parent's network processing (handles RS network
   * tasks)
   * 2. updateActiveness() - checks if our active state changed and triggers
   * activenessChanged() if it did
   *
   * Why we need updateActiveness():
   * - activenessChanged() only fires when the state CHANGES
   * - But we need something to CHECK if it changed
   * - updateActiveness() does that check every tick
   *
   * Flow: doWork() → updateActiveness() → (if changed) → activenessChanged() →
   * update POWERED property
   */
  @Override
  public void doWork() {
    // Run the parent's tick logic (handles RS network node processing)
    super.doWork();

    // Safety check: only update activeness if the world is fully loaded
    // During world loading, level might not be fully initialized yet
    if (level != null && !level.isClientSide()) {
      // Check if our active state should change based on network status
      // If it changed, this will call activenessChanged() automatically
      updateActiveness(getBlockState(), ExternalStorageInterfaceBlock.POWERED);
    }
  }

  /**
   * Returns the display name of this block entity.
   *
   * This is used when showing the block name in:
   * - The UI (if we add a screen later)
   * - Tooltips
   * - Chat messages
   *
   * Component.translatable means it supports different languages.
   * It looks up the translation in your language files
   * (resources/assets/create_refined/lang/en_us.json)
   *
   * @return The translatable name component for this block
   */
  @Override
  public Component getName() {
    return Component.translatable("block." + RefinedIntegrations.MODID + "."
        + ExternalStorageInterfaceBlock.BLOCK_NAME);
  }

}
