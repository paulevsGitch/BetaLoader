<p align="center">
  <img src="https://github.com/paulevsGitch/BetaLoader/blob/main/logo.png" />
</p>

BetaLoader is a port of Risugami's ModLoader, ModLoaderMP and Forge for Station API (STAPI), for Minecraft beta 1.7.3.
Current realisation can't launch any ModLoader mod, only mods without base edits (which are installed by placing them into mods folder).

Some functions are incomplete, if you see any bugs please report them into issues.
Forge version is 1.0.6.

### Credits:
- Modloader and ModloaderMP were made by Risugami: https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1272333-risugamis-mods-updated
- Forge was made by (from inner credits list):
  * Eloraam
  * FlowerChild
  * Hawkye
  * MALfunction84
  * Scokeev9
  * SpaceToad


- Forge credits are located in [minecraftforge_credits.txt](src/main/java/forge/minecraftforge_credits.txt)
- Forge license is located in [forge-licence.txt](forge-licence.txt)

### How It Works:
- BetaLoader will download some necessary libraries on startup (Minecraft Client and Javassist). This is done this way due to ARR license;
- BetaLoader will load all ModLoader mods from mods folder and remap them with Tiny Remapper for STAPI mappings. Some mappings will be generated during runtime;
- Mods will be loaded into game (not as instances of STAPI mods, only as classes).

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

### Mods that will work (tested):
Some mods were taken from [Station API Discord](https://discord.gg/8Qky5XY)
- [AirShip](https://archive.org/download/minecraftbeta1.7.3modarchive/Minecraft%20Beta%201.7.3%20Mod%20Archive.zip/airshipV2.51_b173.zip)
- [ArmorStand](https://archive.org/download/minecraftbeta1.7.3modarchive/Minecraft%20Beta%201.7.3%20Mod%20Archive.zip/ArmorStand_b173.zip)
- [Blackstone v1.1](https://github.com/LO6AN/MC-Addons/raw/main/Beta%201.7.3/%5BBeta%201.7.3%5D%20Blackstone%20v1.1.zip)
- [Clay Soliders](https://archive.org/download/minecraftbeta1.7.3modarchive/Minecraft%20Beta%201.7.3%20Mod%20Archive.zip/ClaySoldierModV3_b173.zip)
- [ElementalCreepers](https://b2.mcarchive.net/file/mcarchive/340befa2684a7336d8d5e7f418cc6e88c945f5596ab3edf73b4e157b527e09e7/ElementalCreepers_v1.4.zip)
- [EquivalentExchangev2.08b](https://b2.mcarchive.net/file/mcarchive/fdb6a12235f6cbdf2f1a2f8e5b30ea4f9f1acde7367beec07857506cb7d0ccd5/ee-v2.08b.zip)
- [Fossil](https://b2.mcarchive.net/file/mcarchive/162cdb138c7a1824fa25e3435f0cac7c6a3f2dfd647c066c4875c61d1656bc73/mod_Fossil.zip)
- [IndustrialCraft1v8.50](https://b2.mcarchive.net/file/mcarchive/c3fa2e5d5469638aac0b3daf0e2a9eb6b07047ca39649cd3eff359bfb07d6aa0/IndustrialCraft_v8.50.zip)
- [IronFence](https://b2.mcarchive.net/file/mcarchive/0a0e191464ade1759256f02e4700b5a7009ae1917330936524421acb95f8c7c6/IronFence_1-2_1-7-3.zip)
- [LaunchPad](https://www.mediafire.com/file/7z1n85b7yikqcvn/mod_LaunchPad.zip/file)
- [Marbles](https://b2.mcarchive.net/file/mcarchive/1dc69d82f8367f7cc1ae1a62ce360bd98b9262b5b22eda9862cf35e45ac9d49f/Marbles_1-9_into_mods_folder.zip)
- [MineColony](https://archive.org/download/minecraftbeta1.7.3modarchive/Minecraft%20Beta%201.7.3%20Mod%20Archive.zip/MineColony0.7rc13_b173.zip)
- [MineEssence](https://archive.org/download/minecraftbeta1.7.3modarchive/Minecraft%20Beta%201.7.3%20Mod%20Archive.zip/MineEssence_V1r3_b173.zip)
- [MineFactoryReloaded](http://www.mediafire.com/file/sb6x1zxf627breb/MinefactoryReloaded_Client_1.1.2.zip/file)
- [MoreFurnaces](https://archive.org/download/minecraftbeta1.7.3modarchive/Minecraft%20Beta%201.7.3%20Mod%20Archive.zip/moreFurnaces_1.0_Client-forgeCubeX2_b173.zip)
- [MovableSlot](https://www.mediafire.com/file/fxmskd1ywiwsdzs/mod_MovableSlot.zip/file)
- [Quit Button](https://www.mediafire.com/file/dbstumoktdgjk1v/mod_QuitButton.zip/file)
- [Tinted Glass](https://www.mediafire.com/file/ihkw55b1gvx77sz/Tinted_Glass.zip/file)
- [TropiCraftMP (custom)](https://www.mediafire.com/file/us6ghhj3v77pwtt/TropicraftMP+ClientV1.3.zip)
- [Twilight Forest 1p4](https://www.minezone.pro/download/mods/1142-173the-twilight-forest-v01p4.html)
- [WizardCraft](https://archive.org/download/minecraftbeta1.7.3modarchive/Minecraft%20Beta%201.7.3%20Mod%20Archive.zip/WizardCraft_b173.zip)
- [Wood Chipper](https://www.planetminecraft.com/mod/beta-1-7-3-modloader-modloadermp-wood-chipper/)

Mods that I used for tests from no-public sources:
- Custom Twilight Forest (provided by GameHerobrine)
- TaleOfKingdom (provided by GameHerobrine)
