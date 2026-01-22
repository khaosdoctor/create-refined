package com.khaosdoctor.create_refined.network_interface.datagen;

import com.khaosdoctor.create_refined.CreateRefined;
import com.khaosdoctor.create_refined.network_interface.NetworkInterfaceBlock;

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
    cube(
        NetworkInterfaceBlock.BLOCK_NAME,
        modLoc("block/network_interface_up"),
        modLoc("block/network_interface_down"),
        modLoc("block/network_interface_north"),
        modLoc("block/network_interface_south"),
        modLoc("block/network_interface_east"),
        modLoc("block/network_interface_west")).texture("particle", modLoc("block/network_interface_south"));
  }
}
