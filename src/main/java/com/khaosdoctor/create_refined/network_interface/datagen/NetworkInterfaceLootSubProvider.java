package com.khaosdoctor.create_refined.network_interface.datagen;

import java.util.Set;

import com.khaosdoctor.create_refined.CreateRefined;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class NetworkInterfaceLootSubProvider extends BlockLootSubProvider {
  // The constructor can be private if this class is an inner class of your loot
  // table provider.
  // The parameter is provided by the lambda in the LootTableProvider's
  // constructor.
  public NetworkInterfaceLootSubProvider(HolderLookup.Provider lookupProvider) {
    // The first parameter is a set of blocks we are creating loot tables for.
    // Instead of hardcoding,
    // we use our block registry and just pass an empty set here.
    // The second parameter is the feature flag set, this will be the default flags
    // unless you are adding custom flags (which is beyond the scope of this
    // article).
    super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
  }

  // The contents of this Iterable are used for validation.
  // We return an Iterable over our block registry's values here.
  @Override
  protected Iterable<Block> getKnownBlocks() {
    // The contents of our DeferredRegister.
    return CreateRefined.BLOCKS.getEntries()
        .stream()
        // Cast to Block here, otherwise it will be a ? extends Block and Java will
        // complain.
        .map(e -> (Block) e.value())
        .toList();
  }

  // Actually add our loot tables.
  @Override
  protected void generate() {
    // Equivalent to calling add(CreateRefined.NETWORK_INTERFACE.get(),
    // createSingleItemTable(CreateRefined.NETWORK_INTERFACE.get()));
    dropSelf(CreateRefined.NETWORK_INTERFACE.get());
  }
}
