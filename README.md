# Vortex: NeoForge Mod Usage Analyzer 
<p align="left">
  <a href="https://github.com/jwlashley/atmospace-vortex"><img src="https://badgen.net/badge/Development/Active/green" /></a>
  <a href=""><img src="https://badgen.net/badge/Stable/2.0.0/blue" /></a>
  <a href="https://www.gnu.org/licenses/gpl-3.0.en.html"><img src="https://badgen.net/badge/License/GPL3/red" /></a>
</p>

![vortex banner](vortexbanner.png)


Vortex is a server-side NeoForge mod that helps administrators understand and optimize their modpacks by tracking actual player engagement with modded content. Get clear data to streamline your modpack, reduce server load, and enhance player experience.

## Purpose & Why You Need It

Large modpacks can suffer from unused mods, leading to unnecessary server load and client lag. Vortex provides concrete usage statistics, allowing you to make data-driven decisions to optimize your modpack, ensuring it's lean, efficient, and tailored to your community's actual playstyle.

## Features

* **Modded Content Tracking:** Monitors player interactions with modded blocks, items, crafting, and entities.

* **Vanilla Filter:** Automatically ignores vanilla Minecraft content for focused data.

* **In-Game Summaries:** Use `/vortex` or `/vx` (OP 2+) for instant usage overviews in chat.
  
* **Dataview Web Panel:** Use `/vortex dataview` or `/vx dataview` for a URL that will allow you to view the data that Vortex has collected in an easy to use web-panel.

* **CSV Export:** Generates detailed, timestamped CSV reports (`config/vortex/`) upon server command and shutdown for external analysis.

* **Clear Data Command:** Reset in-memory statistics at any time with `/vortex clear`.

## How It Works (Source Code Overview)

Vortex leverages the NeoForge event system. Its modular design separates concerns into distinct Java classes:

* **`Vortex.java`:** Main mod entry point; handles event bus and command registration.

* **`VortexTracker.java`:** Manages in-memory storage for all usage counts.

* **`VortexEventHandler.java`:** Listens for and processes specific in-game events (interactions, crafting, damage), filtering out vanilla content.

* **`VortexCommands.java`:** Defines and handles the `/vortex` in-game commands.

* **`DataExporter.java`:** Saves all collected usage data to a CSV file when requested or when the server stops.

## Installation (for Server Administrators)

1.  **Download:** Get the latest `vortex.jar` that matches your game version from [Modrinth](https://modrinth.com/mod/atmospace-vortex).

3.  **Place:** Drop the JAR into your NeoForge 1.21.1 server's `mods` folder.

4.  **Run:** Start your server.

## Usage (for Server Administrators)

* **In-Game Summary:** Type `/vortex` or `/vx` (OP level 2+).

* **Dataview:** Type `/vortex dataview` or `/vx dataview` to get a link to the Dataview for your server.

* **Clear Data:** Use `/vortex clear`.

* **See Unused Mods:** Use `/vortex unused`.

* **CSV Reports:** Find `vortex_mod_usage_data_MM-DD-YYYY.csv` in `config/vortex/` after running the `/vx export` server shutdown.

## Building from Source (for Developers)

1.  **Clone:** `git clone https://github.com/jwlashley/atmospace-vortex.git && cd atmospace-vortex`

2.  **Setup:** Ensure NeoForge MDK for Minecraft 1.21.1 and JDK 21 are installed.

3.  **Build:** `./gradlew clean build` (JAR in `build/libs/`).

## Contributing

Contributions are welcome! Report bugs, suggest features, or submit pull requests on the [GitHub repository](https://github.com/jwlashley/atmospace-vortex).

Keep in mind a general sense of professional etiquette when collaborating within this repository. No one should be made to feel lesser due to lack of expertise on a subject.
Please look over the [Contribution Guide](https://github.com/jwlashley/atmospace-vortex/blob/main/CONTRIBUTING.md) or [Community Code of Conduct](https://github.com/jwlashley/atmospace-vortex/tree/main?tab=coc-ov-file) if you need further explanation of this.

## Roadmap

Vortex is currently in a state of active development in which features are still being added to the mod to make it a fully-fledged utility for modded server administrators to utilize on all of their servers.
During this stage of development features may be added, changed, or removed at any time.

The below features are currently planned for development and release:

* **Data expansion:** We are looking into expanding the data that we collect to allow admins to get a better overall picture of their modpack.
* **Vortex-Dataview:** It is in the plans to develop a web panel for admins to be able to explore their data more easily and allow them to get the granularity of data required to make important decisions.

## License

This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html). See the `LICENSE` file for details.
