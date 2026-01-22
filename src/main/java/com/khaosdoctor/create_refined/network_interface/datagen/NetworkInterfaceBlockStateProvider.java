package com.khaosdoctor.create_refined.network_interface.datagen;

import com.khaosdoctor.create_refined.CreateRefined;
import com.khaosdoctor.create_refined.network_interface.NetworkInterfaceBlock;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class NetworkInterfaceBlockStateProvider extends BlockStateProvider {

  public NetworkInterfaceBlockStateProvider(
      PackOutput output,
      ExistingFileHelper existingFileHelper) {
    super(output, CreateRefined.MODID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    simpleBlock(CreateRefined.NETWORK_INTERFACE.get(),
        models().getExistingFile(modLoc(String.format("block/%s", NetworkInterfaceBlock.BLOCK_NAME))));
  }
}
