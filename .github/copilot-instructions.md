# AI Coding Agent Quickstart – Refined Integrations

- Purpose: NeoForge 1.21.1 mod that bridges Create and Refined Storage; template mod with server/client separation and data-gen.

## Core Files
- Main mod entry: [src/main/java/com/khaosdoctor/refined_integrations/RefinedIntegrations.java](src/main/java/com/khaosdoctor/refined_integrations/RefinedIntegrations.java) – `@Mod(MODID)`, registers blocks/items/tabs via `DeferredRegister`, hooks `FMLCommonSetupEvent`, registers config, and listens on `NeoForge.EVENT_BUS`.
- Client entry: [src/main/java/com/khaosdoctor/refined_integrations/RefinedIntegrationsClient.java](src/main/java/com/khaosdoctor/refined_integrations/RefinedIntegrationsClient.java) – `@Mod(dist=CLIENT)`; registers config screen via `IConfigScreenFactory`; client events via `@EventBusSubscriber(Dist.CLIENT)` and `FMLClientSetupEvent`.
- Config: [src/main/java/com/khaosdoctor/refined_integrations/Config.java](src/main/java/com/khaosdoctor/refined_integrations/Config.java) – `ModConfigSpec` fields (bool/int/string/list) with `BuiltInRegistries` validation for item IDs.

## Build & Run
- Java 21 required; NeoForge 21.1.216; Create 6.0.9-215, Ponder 1.0.81, Flywheel 1.0.6, Registrate MC1.21-1.3.0+67, Refined Storage 2.0.0 (see [gradle.properties](gradle.properties)).
- Commands: `./gradlew runClient`, `./gradlew runServer`, `./gradlew runData`, `./gradlew clean`, `./gradlew --refresh-dependencies`.
- Data gen outputs to `src/generated/resources/`; resources included via `sourceSets.main.resources { srcDir 'src/generated/resources' }`.

## Patterns & Conventions
- Registration: keep IDs lowercase_with_underscores; register deferred blocks/items/tabs in constructor (`BLOCKS/ITEMS/CREATIVE_MODE_TABS.register(modEventBus)`).
- Creative tabs: use `CreativeModeTab.builder()` with `.withTabsBefore` to order; icon is a supplier from registered item.
- Events: static handlers via `@EventBusSubscriber`; instance handlers require `NeoForge.EVENT_BUS.register(this)`.
- Client/server split: client-only code lives in `RefinedIntegrationsClient`; do not call client classes from common code.
- Logging: `LogUtils.getLogger()` → `RefinedIntegrations.LOGGER`; debug markers configured in `build.gradle` run configs.
- Config: register once in constructor (`modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)`); keep translations in `assets/refined_integrations/lang/en_us.json`.

## Integration Notes
- Create & Registrate: use `DeferredBlock`/`DeferredItem` helpers; avoid transitive deps on Create slim artifact (`transitive = false`).
- Flywheel: API is compileOnly; implementation is runtimeOnly.
- Refined Storage brought via CurseMaven artifact `243076:7039043`.

## Common Tasks
- Add block/item: declare `DeferredBlock` + matching block item in `RefinedIntegrations`; register in constructor; add lang + models via data gen (`runData`).
- Add config option: define in `Config`, ensure translation string, and wire via existing `Config.SPEC` registration.
- Client feature: place in `RefinedIntegrationsClient`; subscribe with `@SubscribeEvent` (Dist.CLIENT) and avoid server class references.

## Metadata
- Mod metadata is templated at build: [src/main/templates/META-INF/neoforge.mods.toml](src/main/templates/META-INF/neoforge.mods.toml) expanded by `generateModMetadata` task.
