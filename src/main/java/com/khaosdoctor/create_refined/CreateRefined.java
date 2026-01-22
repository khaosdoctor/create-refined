package com.khaosdoctor.create_refined;

import org.slf4j.Logger;
import com.khaosdoctor.create_refined.network_interface.*;
import com.khaosdoctor.create_refined.network_interface.datagen.*;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateRefined.MODID)
public class CreateRefined {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "create_refined";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "create_refined" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "create_refined" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "create_refined" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "create_refined:network_interface", combining
    // the namespace and path
    public static final DeferredBlock<Block> NETWORK_INTERFACE = BLOCKS.registerBlock(NetworkInterfaceBlock.BLOCK_NAME,
                    properties -> new NetworkInterfaceBlock());
    // Creates a new BlockItem with the id "create_refined:network_interface",
    // combining the namespace and path
    public static final DeferredItem<BlockItem> NETWORK_INTERFACE_ITEM = ITEMS
                    .registerSimpleBlockItem(NetworkInterfaceBlock.BLOCK_NAME, NETWORK_INTERFACE);

    // Creates a creative tab with the id "create_refined:blocks" for the example
    // item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS_TAB = CREATIVE_MODE_TABS
            .register("blocks", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.create_refined")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> NETWORK_INTERFACE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                        output.accept(NETWORK_INTERFACE_ITEM.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CreateRefined(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(CreateRefined::onGatherData);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (CreateRefined) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // Register the data providers: textures, models, blockstates, loot tables,
    // recipes, etc.
    public static void onGatherData(GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final var lookupProvider = event.getLookupProvider();
        final PackOutput output = generator.getPackOutput();
        final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeServer()) {
            generator.addProvider(
                            true,
                            new NetworkInterfaceLootTableProvider(output, lookupProvider));
        }

        if (event.includeClient()) {
            generator.addProvider(
                            true,
                            new NetworkInterfaceBlockModelProvider(output, existingFileHelper));
            generator.addProvider(
                            true,
                            new NetworkInterfaceItemModelProvider(output, existingFileHelper));
            generator.addProvider(
                            true,
                            new NetworkInterfaceBlockStateProvider(output, existingFileHelper));
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("[Create: Refined] common setup being initialized");

    }

    @SubscribeEvent
    private void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("[Create: Refined] load complete event received");
    }
}
