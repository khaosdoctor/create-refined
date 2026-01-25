package com.khaosdoctor.refined_integrations.external_storage_interface.datagen;

import com.khaosdoctor.refined_integrations.RefinedIntegrations;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ExternalStorageInterfaceItemModelProvider extends ItemModelProvider {
  public ExternalStorageInterfaceItemModelProvider(PackOutput output,
      ExistingFileHelper existingFileHelper) {
    super(output, RefinedIntegrations.MODID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    final String blockName = RefinedIntegrations.NETWORK_INTERFACE.getId().getPath();
    withExistingParent(RefinedIntegrations.NETWORK_INTERFACE_ITEM.getId().getPath(),
        modLoc(String.format("block/%s_on", blockName)));
  }

}
