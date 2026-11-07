Display Tweaks - Phase 2a (GUI + settings + Flight/Sprint/Fullbright)

BUILD
  Easiest: generate a fresh project at https://fabricmc.net/develop/template
  (Minecraft 1.21.11, Yarn mappings, Java 21), then replace its src/ folder and
  build.gradle / gradle.properties with the ones in this zip. That gives you the
  Gradle wrapper too.

  Then:   ./gradlew build        (Windows: gradlew.bat build)
  Jar:    build/libs/displaytweaks-1.0.0.jar  -> your .minecraft/mods folder
  Needs:  Fabric Loader 0.18.4+ and Fabric API for 1.21.11 in the mods folder.

USE
  Right Shift opens the GUI. Click a tab on the left, click a module to toggle.
  Right click a module for its settings. Working: Flight, Sprint, Fullbright. Others are placeholders.

NO-INSTALL BUILD (GitHub)
  1. Make a free account at github.com, click New repository, name it anything.
  2. Click "uploading an existing file", drag in everything inside this folder
     (including the hidden .github folder), then Commit.
  3. Open the Actions tab, wait for "Build mod" to finish (green check).
  4. Click the run, scroll to Artifacts, download displaytweaks-jar.
  5. Unzip it. Put the jar in .minecraft/mods with Fabric API for 1.21.11.
