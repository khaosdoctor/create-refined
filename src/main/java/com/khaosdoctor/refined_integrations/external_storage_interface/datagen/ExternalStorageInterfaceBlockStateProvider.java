package com.khaosdoctor.refined_integrations.external_storage_interface.datagen;

import com.khaosdoctor.refined_integrations.RefinedIntegrations;
import com.khaosdoctor.refined_integrations.external_storage_interface.ExternalStorageInterfaceBlock;

import net.minecraft.data.PackOutput;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ExternalStorageInterfaceBlockStateProvider extends BlockStateProvider {

  public ExternalStorageInterfaceBlockStateProvider(
      PackOutput output,
      ExistingFileHelper existingFileHelper) {
    super(output, RefinedIntegrations.MODID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    var block = RefinedIntegrations.NETWORK_INTERFACE.get();
    String blockName = RefinedIntegrations.NETWORK_INTERFACE.getId().getPath();

    ModelFile offModelFile = models()
        .getExistingFile(modLoc(String.format("block/%s_off", blockName)));

    ModelFile onModelFile = models()
        .getExistingFile(modLoc(String.format("block/%s_on", blockName)));

    getVariantBuilder(block)
        .forAllStates(state -> {
          boolean powered = state.getValue(ExternalStorageInterfaceBlock.POWERED);
          Direction facing = state.getValue(ExternalStorageInterfaceBlock.FACING);

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
