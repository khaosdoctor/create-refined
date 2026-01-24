<div align="center">
  <h1>Create: Refined</h1>
  <p>A bridge between <a href="https://github.com/Creators-of-Create/Create">Create</a> and <a href="https://github.com/refinedmods/refinedstorage2">Refined Storage 2</a></p>

  [![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
  [![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-green)]()
  [![NeoForge](https://img.shields.io/badge/NeoForge-21.1.216-orange)]()
</div>

## About

**Create: Refined** seamlessly integrates Create's mechanical automation with Refined Storage's network-based item storage. This mod adds the **Network Interface** block, which exposes your entire Refined Storage network as a standard inventory that Create (and other mods) can interact with directly.

> This mod was built with a HEAVY help from AI since I have never coded Java before, and I also had never worked with Minecraft modding APIs. So, even though I have written all the code myself (not automated) and tried to fix and test most of the things to make sure they work, if you find any issues, please open an issue or a PR to help me improve it! (and please teach me Java and Minecraft modding too :))

> This is my first Minecraft mod, so please be kind :) and this is also in HEAVY development, so expect frequent updates and changes.

### For Developers

The Network Interface implements `IItemHandler` (NeoForge's standard inventory interface):

```java
// Dynamic slot count based on network contents
int getSlots() // Returns: (item types in network) + 1

// Items are exposed as "virtual slots"
// Slot 0 = First item type, Slot 1 = Second item type, etc.
// Last slot is always empty for new item insertions

// Insert operations ignore slot parameter and add to network
ItemStack insertItem(int slot, ItemStack stack, boolean simulate)

// Extract operations pull from the specific item type in that slot
ItemStack extractItem(int slot, int amount, boolean simulate)
```

**Performance Optimizations:**
- 1-second slot count cache to prevent excessive network queries
- Lazy initialization of the item handler
- Stream-based filtering for item resources vs. fluids

## Building

Clone the repository and import the Gradle project:

```bash
git clone https://github.com/yourusername/create-refined.git
cd create-refined
./gradlew build
```

**Development Commands:**
```bash
./gradlew runClient     # Launch Minecraft client
./gradlew runServer     # Launch dedicated server
./gradlew runData       # Generate assets/data
```

## Dependencies

| Mod | Version | Type |
|-----|---------|------|
| **Minecraft** | 1.21.1 | Required |
| **NeoForge** | 21.1.216 | Required |
| **Create** | 6.0.9-215 | Required |
| **Refined Storage** | 2.0.0 | Required |
| Ponder | 1.0.81 | Required (Create dependency) |
| Flywheel | 1.0.6 | Required (Create dependency) |
| Registrate | MC1.21-1.3.0+67 | Required (Create dependency) |

## Project Structure

```
create_refined/
├── network_interface/           # Main feature package
│   ├── NetworkInterfaceBlock.java
│   ├── NetworkInterfaceBlockEntity.java
│   ├── rs_integration/
│   │   ├── NetworkInterfaceNetworkNode.java
│   │   └── NetworkItemHandler.java  # IItemHandler adapter
│   └── datagen/                     # Data generators
└── Config.java                      # Mod configuration
```

## Credits

This mod bridges two amazing projects:

- **[Create](https://github.com/Creators-of-Create/Create)** by simibubi and team
- **[Refined Storage](https://github.com/refinedmods/refinedstorage2)** by refinedmods

Special thanks to both communities for their excellent APIs and documentation!

## License

This project is licensed under the GNU General Public License v3.0 - see [LICENSE](LICENSE) for details.

---

<div align="center">
  <sub>Built with ❤️ for the Minecraft modding community</sub>
</div>
