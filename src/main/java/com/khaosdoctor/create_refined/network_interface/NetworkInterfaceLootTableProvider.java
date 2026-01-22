package com.khaosdoctor.create_refined.network_interface;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class NetworkInterfaceLootTableProvider extends LootTableProvider {
  public NetworkInterfaceLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, Set.of(),
        List.of(new SubProviderEntry(NetworkInterfaceLootSubProvider::new, LootContextParamSets.BLOCK)), registries);
  }

}
