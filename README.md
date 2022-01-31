<p align="center">
  <img src="https://github.com/paulevsGitch/BetaLoader/blob/main/logo.png" />
</p>

BetaLoader is a port of Risugami's ModLoader and ModLoaderMP for Station API (STAPI), for Minecraft beta 1.7.3.
Current realisation can't launch any ModLoader mod, only mods without base edits (which are installed by placing them into mods folder).

### How It Works:
- BetaLoader will download some necessary libraries on startup (Minecraft Client and Javassist). This is done this way due to ARR license;
- BetaLoader will load all ModLoader mods from mods folder and remap them with Tiny Remapper for STAPI mappings. Some mappings will be generated during runtime;
- Mods will be loaded into game (not as instances of STAPI mods, only as classes).

### Mods that will work (tested):
Some mods were taken from [Station API Discord](https://discord.gg/8Qky5XY)
- [Twilight Forest 1p4](https://www.minezone.pro/download/mods/1142-173the-twilight-forest-v01p4.html)
- [Wood Chipper](https://www.planetminecraft.com/mod/beta-1-7-3-modloader-modloadermp-wood-chipper/)
- [Blackstone v1.1](https://github.com/LO6AN/MC-Addons/raw/main/Beta%201.7.3/%5BBeta%201.7.3%5D%20Blackstone%20v1.1.zip)
- [LaunchPad](https://www.mediafire.com/file/7z1n85b7yikqcvn/mod_LaunchPad.zip/file)
- [MovableSlot](https://www.mediafire.com/file/fxmskd1ywiwsdzs/mod_MovableSlot.zip/file)
- [Tinted Glass](https://www.mediafire.com/file/ihkw55b1gvx77sz/Tinted_Glass.zip/file)
- [Quit Button](https://www.mediafire.com/file/dbstumoktdgjk1v/mod_QuitButton.zip/file)

Mods that I used for tests from no-public sources:
- Custom Twilight Forest (provided by GameHerobrine)
- TaleOfKingdom (provided by GameHerobrine)
- WizardCraft (provided by GameHerobrine)

### Plans For Enhancements (TODO):
- Handle access for some mods that uses protected Minecraft classes;
- More documentation;
- More replacements of ModLoader code to STAPI code (block registering, event handling and so on).

### Links That BetaLoader Uses:
- Minecraft Client: https://launcher.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar
- Javassist: https://repo1.maven.org/maven2/org/javassist/javassist/3.28.0-GA/javassist-3.28.0-GA.jar

### Where I Get ModLoader:
- [ModLoader](http://www.mediafire.com/?jc2n88a51xdfd) (official link + javadocs)
- [ModLoaderMP](https://mcarchive.net/mods/modloadermp?gvsn=b1.7.3)

### My ModLoader Mappings:
I made my own mappings for ModLoader and ModLoader MP in Enigma format, they are located in "mappings" folder in this project.
