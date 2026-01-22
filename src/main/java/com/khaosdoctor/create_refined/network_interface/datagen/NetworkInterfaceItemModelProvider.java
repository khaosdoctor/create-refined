package com.khaosdoctor.create_refined.network_interface.datagen;

import com.khaosdoctor.create_refined.CreateRefined;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class NetworkInterfaceItemModelProvider extends ItemModelProvider {
  public NetworkInterfaceItemModelProvider(PackOutput output,
      ExistingFileHelper existingFileHelper) {
    super(output, CreateRefined.MODID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    withExistingParent(CreateRefined.NETWORK_INTERFACE_ITEM.getId().getPath(),
        modLoc("block/network_interface"));
  }

}
