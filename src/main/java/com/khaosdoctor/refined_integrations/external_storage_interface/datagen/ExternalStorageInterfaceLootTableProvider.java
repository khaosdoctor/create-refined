package com.khaosdoctor.refined_integrations.external_storage_interface.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class ExternalStorageInterfaceLootTableProvider extends LootTableProvider {
  public ExternalStorageInterfaceLootTableProvider(PackOutput output,
      CompletableFuture<HolderLookup.Provider> registries) {
    super(output, Set.of(),
        List.of(new SubProviderEntry(ExternalStorageInterfaceLootSubProvider::new, LootContextParamSets.BLOCK)),
        registries);
  }

}
