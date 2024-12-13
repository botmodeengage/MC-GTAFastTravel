# FastTravel

i saw a tweet that showed gta style fast travel in minecraft but it only works in bedrock edition and i want it to work in java edition so here is an over engineered plugin that will do it

## Features

- **Teleport to Coordinates**: travel to specified X, Y, Z coordinates.
- **Teleport to Players**: teleport to other online players.
- **Asynchronous Chunk Loading**: efficiently load necessary chunks (hopefully) without server lag.

## Installation

1. **Download the Plugin**:
   - Get the latest `FastTravel.jar` from the [Releases](https://github.com/botmodeengage/MC-GTAFastTravel/releases) section.

2. **Place the JAR**:
   - Move `FastTravel.jar` to your server's `plugins` directory.

3. **Install Dependencies**:
   - Ensure the following plugins are installed and enabled:
     - [Citizens](https://www.spigotmc.org/resources/citizens.13811/)
     - [PaperLib](https://github.com/PaperMC/PaperLib)
     - [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

4. **Restart the Server**:
   - Restart your Minecraft server to load FastTravel.

## Usage

### Commands

- **Teleport to Coordinates**:
  /ft <x> <y> <z>
  Teleports you to the specified coordinates.

- **Teleport to Player**:
/ft <playerName>
Teleports you to the specified player's location.

### Example

/ft -456 63 -128
Teleports you to coordinates (-456, 63, -128).

## Dependencies

- **[Citizens](https://www.spigotmc.org/resources/citizens.13811/)**: Provides NPC integration, allowing the creation of dynamic teleportation points.
- **[PaperLib](https://github.com/PaperMC/PaperLib)**: Facilitates asynchronous chunk loading to ensure smooth performance without server lag.
- **[ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)**: Enhances compatibility and performance by allowing advanced protocol features.

## Permissions

- **`fasttravel.use`**: Allows players to use fast travel commands.
- **`fasttravel.admin`**: Grants access to administrative functions and configurations.

## License

This project is licensed under the [MIT License](LICENSE)

## Support

For issues please open an issue on the [GitHub Issues](https://github.com/botmodeengage/MC-GTAFastTravel/issues) page
