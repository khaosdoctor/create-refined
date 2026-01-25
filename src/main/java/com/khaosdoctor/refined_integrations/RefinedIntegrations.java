package com.khaosdoctor.refined_integrations;

import org.slf4j.Logger;

import com.khaosdoctor.refined_integrations.external_storage_interface.*;
import com.khaosdoctor.refined_integrations.external_storage_interface.datagen.*;
import com.mojang.logging.LogUtils;
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(RefinedIntegrations.MODID)
public class RefinedIntegrations {
        // Define mod id in a common place for everything to reference
        public static final String MODID = "refined_integrations";
        // Directly reference a slf4j logger
        public static final Logger LOGGER = LogUtils.getLogger();
        // Create a Deferred Register to hold Blocks which will all be registered under
        // the "refined_integrations" namespace
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
        // Create a Deferred Register to hold Items which will all be registered under
        // the "refined_integrations" namespace
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
        // Create a deferred register to hold all the block entities
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(Registries.BLOCK_ENTITY_TYPE, MODID);
        // Create a Deferred Register to hold CreativeModeTabs which will all be
        // registered under the "refined_integrations" namespace
        public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
                        .create(Registries.CREATIVE_MODE_TAB, MODID);

        // Creates a new Block with the id
        // "refined_integrations:external_storage_interface", combining
        // the namespace and path
        public static final DeferredBlock<Block> NETWORK_INTERFACE = BLOCKS.registerBlock(
                        ExternalStorageInterfaceBlock.BLOCK_NAME,
                        properties -> new ExternalStorageInterfaceBlock());

        // Creates a new BlockItem with the id
        // "refined_integrations:external_storage_interface",
        // combining the namespace and path
        public static final DeferredItem<BlockItem> NETWORK_INTERFACE_ITEM = ITEMS
                        .registerSimpleBlockItem(ExternalStorageInterfaceBlock.BLOCK_NAME, NETWORK_INTERFACE);

        // Creates the block entity type for the External Storage Interface Block Entity
        @SuppressWarnings("null")
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExternalStorageInterfaceBlockEntity>> NETWORK_INTERFACE_BLOCK_ENTITY = BLOCK_ENTITIES
                        .register(NETWORK_INTERFACE.getId().getPath(),
                                        () -> BlockEntityType.Builder
                                                        .of(ExternalStorageInterfaceBlockEntity::new,
                                                                        NETWORK_INTERFACE.get())
                                                        .build(null));

        // Creates a creative tab with the id "refined_integrations:blocks" for the
        // example
        // item, that is placed after the combat tab
        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS_TAB = CREATIVE_MODE_TABS
                        .register("blocks", () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.refined_integrations")) // The language
                                                                                                         // key for the
                                                                                                         // title of
                                                                                                         // your
                                                                                                         // CreativeModeTab
                                        .withTabsBefore(CreativeModeTabs.COMBAT)
                                        .icon(() -> NETWORK_INTERFACE_ITEM.get().getDefaultInstance())
                                        .displayItems((parameters, output) -> {
                                                output.accept(NETWORK_INTERFACE_ITEM.get());
                                        }).build());

        // The constructor for the mod class is the first code that is run when your mod
        // is loaded.
        // FML will recognize some parameter types like IEventBus or ModContainer and
        // pass them in automatically.
        public RefinedIntegrations(IEventBus modEventBus, ModContainer modContainer) {
                // Register the commonSetup method for modloading
                modEventBus.addListener(this::commonSetup);
                modEventBus.addListener(RefinedIntegrations::onGatherData);
                modEventBus.addListener(RefinedIntegrations::onRegisterCapabilities);

                // Register the Deferred Register to the mod event bus so blocks get registered
                BLOCKS.register(modEventBus);
                // Register the Deferred Register to the mod event bus so items get registered
                ITEMS.register(modEventBus);
                // Register the Deferred Register to the mod event bus so tabs get registered
                CREATIVE_MODE_TABS.register(modEventBus);
                // Register the Deferred Register to the mod event bus so block entities get
                // registered
                BLOCK_ENTITIES.register(modEventBus);

                // Register ourselves for server and other game events we are interested in.
                // Note that this is necessary if and only if we want *this* class
                // (RefinedIntegrations) to respond directly to events.
                // Do not add this line if there are no @SubscribeEvent-annotated functions in
                // this class, like onServerStarting() below.
                NeoForge.EVENT_BUS.register(this);

                // Register our mod's ModConfigSpec so that FML can create and load the config
                // file for us
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
                                        new ExternalStorageInterfaceLootTableProvider(output, lookupProvider));
                        generator.addProvider(true, new ExternalStorageInterfaceRecipeProvider(output, lookupProvider));
                }

                if (event.includeClient()) {
                        generator.addProvider(
                                        true,
                                        new ExternalStorageInterfaceBlockModelProvider(output, existingFileHelper));
                        generator.addProvider(
                                        true,
                                        new ExternalStorageInterfaceItemModelProvider(output, existingFileHelper));
                        generator.addProvider(
                                        true,
                                        new ExternalStorageInterfaceBlockStateProvider(output, existingFileHelper));
                }
        }

        /**
         * Registers capabilities for our mod.
         *
         * What are capabilities?
         * - In NeoForge, capabilities are how mods expose functionality to each other
         * - Think of it like an API: "I can do X, ask me for capability X"
         * - Other mods can query: "Does this block have capability X?"
         *
         * Why do we need this for RS integration?
         * - RS needs to find network nodes in the world
         * - It does this by asking blocks: "Do you have a NetworkNodeContainerProvider
         * capability?"
         * - Without registering this capability, RS won't know our block can be a
         * network node
         * - Even though we extend AbstractBaseNetworkNodeContainerBlockEntity, we still
         * need to
         * register the capability so RS can find us!
         *
         * What happens here:
         * 1. RS asks our block: "Do you have a NetworkNodeContainerProvider?"
         * 2. NeoForge checks this registry
         * 3. Finds we registered it, calls: blockEntity.getContainerProvider()
         * 4. RS gets our container and adds us to the network
         *
         * CRITICAL: Without this registration, cables won't connect even if everything
         * else is correct!
         */
        private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
                LOGGER.info("[Refined Integrations] Registering capabilities");

                // Register the NetworkNodeContainerProvider capability for our block entity
                // This tells RS: "When you ask our block for a network node container, call
                // getContainerProvider()"
                event.registerBlockEntity(
                                // What capability are we providing? The RS network node container capability
                                RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
                                // Which block entity type provides this capability? Our External Storage
                                // Interface block entity
                                NETWORK_INTERFACE_BLOCK_ENTITY.get(),
                                // How do we get the capability? Call getContainerProvider() on the block entity
                                // (side) parameter is which side is being queried (north, south, etc.) - we
                                // ignore it
                                (blockEntity, side) -> blockEntity.getContainerProvider());

                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, NETWORK_INTERFACE_BLOCK_ENTITY.get(),
                                (blockEntity, side) -> blockEntity.getItemHandler());
        }

        private void commonSetup(FMLCommonSetupEvent event) {
                // Some common setup code
                LOGGER.info("[Refined Integrations] common setup being initialized");

        }

        @SubscribeEvent
        private void onServerStarted(ServerStartedEvent event) {
                LOGGER.info("[Refined Integrations] load complete event received");
        }
}
