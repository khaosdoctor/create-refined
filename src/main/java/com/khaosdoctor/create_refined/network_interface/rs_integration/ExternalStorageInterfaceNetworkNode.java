package com.khaosdoctor.create_refined.network_interface.rs_integration;

import com.khaosdoctor.create_refined.Config;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;

/**
 * The network node representation for the External Storage Interface block.
 *
 * This is the "data" that lives in Refined Storage's network graph.
 * We extend SimpleNetworkNode which is RS's simplest network node
 * implementation.
 *
 * Energy usage: Configurable via config file (default: 2 FE/t)
 * - The energy value is read from Config.NETWORK_INTERFACE_ENERGY
 * - This allows users/modpack makers to adjust the energy cost
 * - Comparison with other RS devices:
 *   - Cable: 0 (passive)
 *   - Importer/Exporter: 1 (simple transfer)
 *   - Detector: 2 (monitoring) ← Default for External Storage Interface
 *   - Constructor/Destructor: 3 (active manipulation)
 *   - Pattern Grid: 5 (complex interface)
 *   - Crafter: 8+ (crafting automation)
 *
 * What the energy requirement does:
 * - If energy > 0: Requires a network with a Controller (Controllers provide energy)
 * - If energy = 0: Works without a controller (always active if connected to cables)
 * - Network must have at least this much FE/t available
 * - If network runs out of energy, this block becomes inactive (POWERED = false)
 * - Multiple External Storage Interfaces consume energy EACH
 */
public class ExternalStorageInterfaceNetworkNode extends SimpleNetworkNode {
  /**
   * Constructor - creates the network node with energy requirements.
   *
   * The energy value is read from the config file, allowing users to
   * customize how much energy this block consumes.
   */
  public ExternalStorageInterfaceNetworkNode() {
    // Read energy consumption from config (default: 2 FE/t)
    // .get() retrieves the current config value
    super(Config.NETWORK_INTERFACE_ENERGY.get());
  }
}
