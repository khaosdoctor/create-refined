package com.khaosdoctor.create_refined.network_interface;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.*;

public class NetworkInterfaceBlock extends Block {
  public NetworkInterfaceBlock() {
    super(Properties.of()
        .destroyTime(Blocks.DIRT.defaultDestroyTime())
        .friction(Blocks.DIRT.getFriction())
        .lightLevel(value -> 1)
        .isRedstoneConductor((state, level, pos) -> false)
        .mapColor(MapColor.STONE));
  }

}
