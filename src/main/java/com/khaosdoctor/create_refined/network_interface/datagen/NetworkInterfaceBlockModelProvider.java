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
    final String blockName = CreateRefined.NETWORK_INTERFACE.getId().getPath();
    cube(
        CreateRefined.NETWORK_INTERFACE.getId().getPath(),
        modLoc(String.format("block/%s_up", blockName)),
        modLoc(String.format("block/%s_down", blockName)),
        modLoc(String.format("block/%s_north", blockName)),
        modLoc(String.format("block/%s_south", blockName)),
        modLoc(String.format("block/%s_east", blockName)),
        modLoc(String.format("block/%s_west", blockName)))
        .texture("particle", modLoc(String.format("block/%s_south", blockName)));
  }
}
