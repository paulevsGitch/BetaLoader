<p align="center">
  <img src="https://github.com/paulevsGitch/BetaLoader/blob/main/logo.png" />
</p>

BetaLoader is a port of Risugami's ModLoader for Station API (STAPI), for Minecraft beta 1.7.3.
Current realisation can't launch any ModLoader mod, only mods without base edits (which are installed by placing them into mods folder).

### How It Works:
- BetaLoader will download some necessary libraries on startup (Minecraft Client and Javassist);
- It will load all ModLoader mods from mods folder;
- All mods will be remapped with Tiny Remapper for STAPI mappings;
- Mods will be loaded into game.

### Mods that will work (tested):
- Twilight Forest 1p4

### Plans For Enhancements (TODO):
- Handle access for some mods that uses protected Minecraft classes;
- More documentation;
- More replacements of ModLoader code to STAPI code (block registering, event hadling and so on).

### Links That BetaLoader Uses:
- Minecraft Client: https://launcher.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar
- Javassist: https://repo1.maven.org/maven2/org/javassist/javassist/3.28.0-GA/javassist-3.28.0-GA.jar