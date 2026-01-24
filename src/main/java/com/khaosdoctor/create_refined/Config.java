package com.khaosdoctor.create_refined;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration for Create: Refined mod.
 *
 * This class defines config options that users can modify.
 * Config files are generated at: run/config/create_refined-common.toml
 *
 * Translation vs Component.translatable():
 * - .translation("key") on config builder: Used for config GUI displays
 * - Component.translatable("key"): Used for runtime text (chat, tooltips, etc.)
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * Energy consumption for the Network Interface block.
     *
     * The .translation() method tells config GUIs which translation key to use.
     * The actual translated text is in: resources/assets/create_refined/lang/en_us.json
     *
     * NOTE: Changes require rejoining the world (singleplayer) or restarting the server.
     */
    public static final ModConfigSpec.IntValue NETWORK_INTERFACE_ENERGY = BUILDER
                    .comment("Energy consumption (FE/t) for the Network Interface block.",
                                    "This block bridges Create and Refined Storage systems.",
                                    "Comparison: Cable=0, Importer=1, Detector=2, Constructor=3, Pattern Grid=5",
                                    "Default: 2 (monitoring level)",
                                    "NOTE: Changes take effect after rejoining the world or restarting the server.")
                    .translation("create_refined.configuration.networkInterfaceEnergy")
                    .defineInRange("networkInterfaceEnergy", 2, 0, 100);

    static final ModConfigSpec SPEC = BUILDER.build();
}
