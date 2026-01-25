# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NeoForge 1.21.1 mod that bridges Create and Refined Storage. The mod provides an **External Storage Interface** block that exposes Refined Storage networks as standard inventories (`IItemHandler`), allowing Create's mechanical automation (and other mods) to interact with RS storage directly.

### Core Concept

The External Storage Interface implements the **Adapter Pattern** to translate between two different APIs:
- **IItemHandler** (NeoForge's standard inventory interface used by Create, hoppers, pipes, etc.)
- **StorageNetworkComponent** (Refined Storage's network storage API)

This allows Create contraptions to insert/extract items from RS networks as if they were regular chests, without either mod needing to know about the other.

## Build Commands

- `./gradlew runClient` - Launch Minecraft client with the mod
- `./gradlew runServer` - Launch dedicated server (with `--nogui`)
- `./gradlew runData` - Generate assets (models, blockstates, loot tables, etc.) to `src/generated/resources/`
- `./gradlew clean` - Clean build artifacts
- `./gradlew --refresh-dependencies` - Refresh Gradle dependencies

**Important**: Always run `./gradlew runData` after adding/modifying blocks or items to generate assets. Data gen outputs are version-controlled via `sourceSets.main.resources { srcDir 'src/generated/resources' }` in [build.gradle](build.gradle).

## Core Architecture

### Entry Points

- **Main mod class**: [src/main/java/com/khaosdoctor/refined_integrations/RefinedIntegrations.java](src/main/java/com/khaosdoctor/refined_integrations/RefinedIntegrations.java)
  - Annotated with `@Mod(MODID)`
  - Contains `DeferredRegister` instances for blocks, items, and creative tabs
  - Registers data generation providers via `onGatherData()`
  - Subscribes to `FMLCommonSetupEvent` and game events via `NeoForge.EVENT_BUS`

- **Client-only code**: [src/main/java/com/khaosdoctor/refined_integrations/RefinedIntegrationsClient.java](src/main/java/com/khaosdoctor/refined_integrations/RefinedIntegrationsClient.java)
  - Annotated with `@Mod(dist=CLIENT)` to prevent loading on dedicated servers
  - Handles client setup and config screen registration
  - Uses `@EventBusSubscriber(Dist.CLIENT)` for client events

- **Configuration**: [src/main/java/com/khaosdoctor/refined_integrations/Config.java](src/main/java/com/khaosdoctor/refined_integrations/Config.java)
  - `ModConfigSpec` with validation (e.g., `validateItemName` uses `BuiltInRegistries`)
  - Registered once in main constructor: `modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)`

### Feature Organization Pattern

Features are organized in dedicated packages under the main package (e.g., `network_interface/`):

```
com.khaosdoctor.refined_integrations/
├── RefinedIntegrations.java          # Main mod class with DeferredRegister instances
├── RefinedIntegrationsClient.java    # Client-only entry point
├── Config.java                 # Mod configuration
└── <feature_name>/             # Feature package (e.g., network_interface)
    ├── <Feature>Block.java     # Block implementation
    ├── <Feature>BlockEntity.java (optional)
    └── datagen/                # Data generators for this feature
        ├── <Feature>BlockStateProvider.java
        ├── <Feature>BlockModelProvider.java
        ├── <Feature>ItemModelProvider.java
        ├── <Feature>LootTableProvider.java
        └── <Feature>LootSubProvider.java
```

**Key pattern**: Each feature gets its own package with dedicated data generation providers. All registration happens in the main `RefinedIntegrations` class via `DeferredRegister`.

### External Storage Interface Implementation

The `network_interface/` package demonstrates integration with both Create and Refined Storage:

```
network_interface/
├── ExternalStorageInterfaceBlock.java           # Block with FACING and POWERED properties
├── ExternalStorageInterfaceBlockEntity.java     # Extends AbstractBaseNetworkNodeContainerBlockEntity<T>
├── rs_integration/
│   ├── ExternalStorageInterfaceNetworkNode.java # RS network node (extends SimpleNetworkNode)
│   └── NetworkItemHandler.java                  # IItemHandler adapter (Adapter Pattern)
└── datagen/                                     # Standard data generators
```

#### Key Implementation Details

**ExternalStorageInterfaceBlockEntity:**
- Extends `AbstractBaseNetworkNodeContainerBlockEntity<ExternalStorageInterfaceNetworkNode>` from RS
- This provides automatic RS network integration (joining/leaving networks, energy management)
- Contains a lazy-initialized `NetworkItemHandler` instance
- Exposes handler via `getItemHandler()` method for capability system
- Uses `mainNetworkNode` (inherited protected field) to access the RS network node

**NetworkItemHandler (The Adapter):**
- Implements `IItemHandler` interface
- Wraps an `ExternalStorageInterfaceNetworkNode` reference
- **Dynamic slot count**: Returns `(item types in network) + 1` from `getSlots()`
  - The +1 ensures there's always an empty slot for inserting new item types
  - Without this, Create would refuse to insert items not already in the network
- **1-second cache** on slot count to prevent excessive network queries
- **Insert operations**: Ignore slot parameter, insert directly into network via `storage.insert()`
- **Extract operations**: Map slot index to Nth item type in network, extract via `storage.extract()`
- **Virtual slots**: Slots are "windows" into the network, not actual storage

**Performance Considerations:**
```java
// Cached slot calculation (1-second TTL)
@Override
public int getSlots() {
    if (currentTime - lastCacheTime >= 1000) {
        cachedSlotCount = calculateSlots() + 1;
        lastCacheTime = currentTime;
    }
    return cachedSlotCount;
}

// Stream-based filtering for item resources
private int calculateSlots() {
    Collection<ResourceAmount> resources = storage.getAll();
    return (int) resources.stream()
        .filter(res -> res.resource() instanceof ItemResource)
        .count();
}
```

### Registration Flow

1. Declare `DeferredBlock`/`DeferredItem` as static finals in `RefinedIntegrations`
2. Register them in the constructor: `BLOCKS.register(modEventBus)`
3. Reference in creative tabs via suppliers: `() -> ITEM.get().getDefaultInstance()`
4. Add translations to `assets/refined_integrations/lang/en_us.json`
5. Generate assets via `./gradlew runData`

### Capability Registration

Capabilities are NeoForge's way of exposing functionality between mods. The External Storage Interface registers **two capabilities**:

**1. RS Network Node Container Provider** (so RS cables can connect):
```java
event.registerBlockEntity(
    RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
    NETWORK_INTERFACE_BLOCK_ENTITY.get(),
    (blockEntity, side) -> blockEntity.getContainerProvider()
);
```

**2. ItemHandler** (so Create/hoppers can access inventory):
```java
event.registerBlockEntity(
    Capabilities.ItemHandler.BLOCK,
    NETWORK_INTERFACE_BLOCK_ENTITY.get(),
    (blockEntity, side) -> blockEntity.getItemHandler()
);
```

**CRITICAL**: Both registrations happen in `RefinedIntegrations.onRegisterCapabilities()`. Without these, the block won't function - RS cables won't connect AND Create won't see the inventory.

### Data Generation

Data providers are registered in `RefinedIntegrations.onGatherData()`:
- Server-side: Loot tables
- Client-side: Block states, block models, item models

Each feature should have its own set of data providers in a `datagen/` subpackage. The data gen task uses `--existing-mod create` and `--existing-mod refinedstorage` to reference parent mod assets.

## Critical Patterns & Conventions

### Naming

- Block/item IDs: `lowercase_with_underscores` (e.g., `external_storage_interface`)
- Java classes: `PascalCase` (e.g., `ExternalStorageInterfaceBlock`)
- Translation keys follow pattern: `block.refined_integrations.<block_name>`

### Client/Server Separation

- **NEVER** call client-only classes from common code
- Client code goes in `RefinedIntegrationsClient` or is annotated with `@EventBusSubscriber(Dist.CLIENT)`
- Use `level.isClientSide()` checks when needed in common code

### Event Handling

- **Mod event bus**: Passed to constructor, used for registration and lifecycle events
- **NeoForge event bus**: Use `NeoForge.EVENT_BUS.register(this)` for game events
- Static handlers: `@EventBusSubscriber` annotation
- Instance handlers: Require explicit registration with `NeoForge.EVENT_BUS.register(this)`

### Block Properties

Blocks should define state properties as static constants:
```java
public static final DirectionProperty FACING = BlockStateProperties.FACING;
public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
```

### Create Integration

- Uses `IWrenchable` interface for wrench support (from Create)
- Block can implement `onWrenched()` to customize wrench behavior
- Return `InteractionResult.PASS` to allow crouch+wrench pickup without rotation

### Refined Storage Integration

**RS Network Node Lifecycle:**
1. BlockEntity extends `AbstractBaseNetworkNodeContainerBlockEntity<T>`
2. Constructor creates a new `ExternalStorageInterfaceNetworkNode` (extends `SimpleNetworkNode`)
3. `clearRemoved()` is called → node joins the RS network automatically
4. `setRemoved()` is called → node leaves the RS network automatically
5. `doWork()` ticks every game tick → calls `updateActiveness()` to check network status

**Network Node → Storage Access Pattern:**
```java
// Get the network
Network network = node.getNetwork();
if (network == null || !node.isActive()) return null;

// Get storage component
StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);

// Insert/extract with Actor tracking
ItemResource resource = ItemResource.ofItemStack(stack);
long inserted = storage.insert(resource, amount, Action.EXECUTE, ACTOR);
long extracted = storage.extract(resource, amount, Action.EXECUTE, ACTOR);
```

**Actor Interface:**
The `Actor` identifies who is performing storage operations (for logging/filtering):
```java
private static final Actor ACTOR = () -> RefinedIntegrations.NETWORK_INTERFACE.getId().toString();
```

**RS API Key Classes:**
- `StorageNetworkComponent` - The storage interface (extends `Storage`)
- `ResourceAmount` - Record containing `resource()` and `amount()`
- `ItemResource` - RS's representation of Minecraft items
- `Action` - Enum: `SIMULATE` (test) or `EXECUTE` (actually do it)
- `SimpleNetworkNode` - Base class for network nodes with energy consumption

## Dependencies

- **Java 21** (required by Minecraft 1.21.1)
- **NeoForge**: 21.1.216
- **Create**: 6.0.9-215 (slim artifact with `transitive = false`)
- **Ponder**: 1.0.81
- **Flywheel**: 1.0.6 (API is `compileOnly`, implementation is `runtimeOnly`)
- **Registrate**: MC1.21-1.3.0+67
- **Refined Storage**: 2.0.0 (via CurseMaven: `curse.maven:refined-storage-243076:7039043`)

All version properties are in [gradle.properties](gradle.properties).

## Metadata

Mod metadata is templated during build:
- Template: `src/main/templates/META-INF/neoforge.mods.toml`
- Expanded by `generateModMetadata` task using properties from `gradle.properties`
- Task runs automatically on IDE sync via `neoForge.ideSyncTask generateModMetadata`

## Common Patterns & Gotchas

### IItemHandler Virtual Inventory Pattern

**Problem**: RS networks don't have "slots" - they have a dynamic list of item types.
**Solution**: Map slot indices to item types: slot 0 = first item, slot 1 = second item, etc.

**Critical Design Decision**: Always expose N+1 slots where N = item types in network
- This ensures there's always an empty slot for new items
- Without this, Create will refuse to insert items not already in the network
- Example: Network has 5 items → expose 6 slots (slots 0-4 show items, slot 5 is empty)

### RS Integration Best Practices

1. **Never access `mainNetworkNode` before `clearRemoved()`**
   - The node isn't fully initialized until the BlockEntity is added to the world
   - Always check `node.isActive()` before accessing the network

2. **Use lazy initialization for IItemHandler**
   - Create the handler only when first requested via capability
   - Avoids issues during BlockEntity construction/deserialization

3. **ItemStack ↔ ItemResource Conversion**
   - To RS: `ItemResource resource = ItemResource.ofItemStack(stack);`
   - From RS: `ItemStack stack = itemResource.toItemStack(amount);`
   - **Note**: RS uses `long` for amounts, Minecraft uses `int` - cast carefully!

4. **Null Annotations Matter**
   - `@Nonnull` on IItemHandler methods is **required** by the interface
   - Return `ItemStack.EMPTY` instead of null
   - Missing annotations cause compilation errors

### BlockEntityType.Builder Null Warning

When registering block entities, you'll see a null warning:
```java
@SuppressWarnings("null")  // Safe: null parameter is standard for no data fixers
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> BE =
    BLOCK_ENTITIES.register("name", () -> BlockEntityType.Builder
        .of(Constructor::new, BLOCK.get())
        .build(null));  // null = no data fixers (standard practice)
```

The `null` parameter is for data fixers (migrating save data between MC versions). For most mods, passing `null` is correct and safe to suppress.

### Performance Tuning

**Slot Count Caching:**
- `getSlots()` can be called frequently by Create/hoppers
- Cache the result with a short TTL (1 second)
- Recalculate only when cache expires

**Stream Operations:**
- Use `.stream().filter()` to separate ItemResources from FluidResources
- RS networks can contain both - filter to items only
- Consider using `.toList()` instead of `.collect(Collectors.toList())` (Java 16+)
