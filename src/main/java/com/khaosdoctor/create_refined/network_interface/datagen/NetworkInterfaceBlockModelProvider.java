package com.khaosdoctor.create_refined.network_interface.datagen;

import com.khaosdoctor.create_refined.CreateRefined;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class NetworkInterfaceBlockModelProvider extends BlockModelProvider {

  public NetworkInterfaceBlockModelProvider(
      PackOutput output,
      ExistingFileHelper existingFileHelper) {
    super(output, CreateRefined.MODID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    cubeAll(
        "network_interface",
        mcLoc("block/iron_block"));
  }
}
