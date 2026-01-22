package com.khaosdoctor.create_refined.network_interface;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.*;

public class NetworkInterfaceBlock extends Block {
  public static final String BLOCK_NAME = "network_interface";

  public NetworkInterfaceBlock() {
    super(Properties.of()
        .destroyTime(Blocks.DIRT.defaultDestroyTime())
        .friction(Blocks.DIRT.getFriction())
        .lightLevel(value -> 1)
        .isRedstoneConductor((state, level, pos) -> false)
        .mapColor(MapColor.STONE));
  }

}
