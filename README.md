<p align="center">
  <img src="https://github.com/paulevsGitch/BetaLoader/blob/main/logo.png" />
</p>

BetaLoader is a port of Risugami's ModLoader for Station API (STAPI), for Minecraft beta 1.7.3.
Current realisation can't launch any ModLoader mod, only mods without base edits (which are installed by placing them into mods folder).

### How It Works:
- BetaLoader will download some necessary libraries on startup (Jar Jar Links and Javassist);
- It will load all ModLoader mods from mods folder;
- All mods will be remapped with Tiny Remapper for STAPI mappings;
- Mods will be loaded into game.

### Mods that will work (tested):
- Twilight Forest 1p4 (in dev only - access problem)

### Plans For Enhancements (TODO):
- Remove Jar Jar and replace it with internal Tiny Remapper;
- Handle access for some mods that uses protected Minecraft classes;
- More documentation;
- More replacements of ModLoader code to STAPI code (block registering, event hadling and so on).