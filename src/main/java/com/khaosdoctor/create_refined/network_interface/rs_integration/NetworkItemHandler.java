package com.khaosdoctor.create_refined.network_interface.rs_integration;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.khaosdoctor.create_refined.CreateRefined;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class NetworkItemHandler implements IItemHandler {
  private final ExternalStorageInterfaceNetworkNode node;
  private static final Actor ACTOR = () -> CreateRefined.NETWORK_INTERFACE.getId().toString();

  // Used for caching slot count to improve performance
  private int cachedSlotCount = 0;
  private long lastCacheTime = 0;

  public NetworkItemHandler(ExternalStorageInterfaceNetworkNode node) {
    this.node = node;
  }

  /**
   * Gets the current slots in the network dynamically based on the stored items.
   *
   * This method can be called frequently, so we cache the result for a short time
   * to improve performance.
   */
  @Override
  public int getSlots() {
    long currentTime = System.currentTimeMillis();
    // If cache is recent (within 1 second), return cached value
    if (currentTime - lastCacheTime >= 1000) {
      // Update cache time
      lastCacheTime = currentTime;
      // Recalculate slot count
      cachedSlotCount = calculateSlots() + 1;
    }

    return cachedSlotCount;
  }

  private int calculateSlots() {
    StorageNetworkComponent storage = getStorage();

    // If no storage, zero slots
    if (storage == null) {
      return 0;
    }

    // Count distinct item resources in storage and return that as slot count
    Collection<ResourceAmount> resources = storage.getAll();
    return (int) resources.stream().filter(res -> res.resource() instanceof ItemResource).count();
  }

  @Override
  public int getSlotLimit(int slot) {
    return 64;
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return getStorage() != null;
  }

  private Network getNetwork() {
    if (node == null || !node.isActive()) {
      return null;
    }

    return node.getNetwork();
  }

  private StorageNetworkComponent getStorage() {
    Network network = getNetwork();
    if (network == null) {
      return null;
    }

    return network.getComponent(StorageNetworkComponent.class);
  }

  @Nonnull
  @Override
  public ItemStack getStackInSlot(int slot) {
    // Get the storage
    StorageNetworkComponent storage = getStorage();
    if (storage == null || slot < 0) {
      return ItemStack.EMPTY;
    }

    // Get all items in the storage
    Collection<ResourceAmount> resources = storage.getAll();
    List<ResourceAmount> resourceList = resources.stream().toList();

    if (slot >= resourceList.size()) {
      return ItemStack.EMPTY;
    }

    // Return the Nth item type based on the slot index
    ResourceAmount resourceAmount = resourceList.get(slot);

    // Check if it's an item resource and not a fluid
    if (resourceAmount.resource() instanceof ItemResource itemResource) {
      // Convert to stack
      ItemStack stack = itemResource.toItemStack(resourceAmount.amount());

      // Cap at max stack size
      if (stack.getCount() > stack.getMaxStackSize()) {
        stack.setCount(stack.getMaxStackSize());
      }

      return stack;
    }
    return ItemStack.EMPTY;
  }

  @Nonnull
  @Override
  public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }

    StorageNetworkComponent storage = getStorage();
    if (storage == null) {
      return stack; // Cannot insert, return full stack
    }

    // Convert ItemStack to ItemResource
    ItemResource itemResource = ItemResource.ofItemStack(stack);

    // Try to insert into storage
    long inserted = storage.insert(itemResource, stack.getCount(), simulate ? Action.SIMULATE : Action.EXECUTE, ACTOR);

    // Return the remainder
    if (inserted < stack.getCount()) {
      ItemStack remainder = stack.copy();
      remainder.setCount((int) (stack.getCount() - inserted));
      return remainder;
    }

    return ItemStack.EMPTY;
  }

  @Nonnull
  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    StorageNetworkComponent storage = getStorage();
    if (storage == null || slot < 0 || amount <= 0) {
      return ItemStack.EMPTY;
    }

    // Get the item type in this slot
    Collection<ResourceAmount> resources = storage.getAll();
    List<ResourceAmount> resourceList = resources.stream().toList();

    // Check if slot is valid
    if (slot >= resourceList.size()) {
      return ItemStack.EMPTY;
    }

    // Get the resource at this slot
    ResourceAmount resourceAmount = resourceList.get(slot);
    if (!(resourceAmount.resource() instanceof ItemResource itemResource)) {
      return ItemStack.EMPTY;
    }

    // Extract from storage
    long extracted = storage.extract(itemResource, amount, simulate ? Action.SIMULATE : Action.EXECUTE, ACTOR);
    if (extracted > 0) {
      // Create ItemStack to return
      ItemStack extractedStack = itemResource.toItemStack((int) extracted);
      return extractedStack;
    }

    return ItemStack.EMPTY;
  }

}
