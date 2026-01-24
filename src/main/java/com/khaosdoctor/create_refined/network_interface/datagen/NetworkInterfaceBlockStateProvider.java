package com.khaosdoctor.create_refined.network_interface.datagen;

import com.khaosdoctor.create_refined.CreateRefined;
import com.khaosdoctor.create_refined.network_interface.NetworkInterfaceBlock;

import net.minecraft.data.PackOutput;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class NetworkInterfaceBlockStateProvider extends BlockStateProvider {

  public NetworkInterfaceBlockStateProvider(
      PackOutput output,
      ExistingFileHelper existingFileHelper) {
    super(output, CreateRefined.MODID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    var block = CreateRefined.NETWORK_INTERFACE.get();
    String blockName = CreateRefined.NETWORK_INTERFACE.getId().getPath();

    ModelFile offModelFile = models()
        .getExistingFile(modLoc(String.format("block/%s_off", blockName)));

    ModelFile onModelFile = models()
        .getExistingFile(modLoc(String.format("block/%s_on", blockName)));

    getVariantBuilder(block)
        .forAllStates(state -> {
          boolean powered = state.getValue(NetworkInterfaceBlock.POWERED);
          Direction facing = state.getValue(NetworkInterfaceBlock.FACING);

          return ConfiguredModel.builder()
              .modelFile(powered ? onModelFile : offModelFile)
              .rotationX(getXRotation(facing))
              .rotationY(getYRotation(facing))
              .build();
        });
  }

  private static int getXRotation(Direction facing) {
    return switch (facing) {
      case DOWN -> 180;
      case UP -> 0;
      default -> 90;
    };
  }

  private static int getYRotation(Direction facing) {
    return switch (facing) {
      case NORTH, UP, DOWN -> 0;
      case EAST -> 90;
      case SOUTH -> 180;
      case WEST -> 270;
    };
  }
}
