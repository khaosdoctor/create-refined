package com.khaosdoctor.create_refined.network_interface.rs_integration;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;

/**
 * The network node representation for the Network Interface block.
 *
 * This is the "data" that lives in Refined Storage's network graph.
 * We extend SimpleNetworkNode which is RS's simplest network node
 * implementation.
 *
 */
public class NetworkInterfaceNetworkNode extends SimpleNetworkNode {
  /**
   * Constructor - creates the network node with energy requirements.
   *
   * The energy value is passed to the parent SimpleNetworkNode class,
   * which handles all the energy tracking and network integration.
   */
  public NetworkInterfaceNetworkNode() {
    super(2); // 2 FE/t
  }
}
