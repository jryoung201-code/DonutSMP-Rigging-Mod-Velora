# Velora 1.1.2 build repair

The supplied archive did not contain `build.gradle`, so Gradle had no Loom build configuration to remap Minecraft/Yarn names into the runtime namespace. This is consistent with the startup crash involving `net.minecraft.client.gui.screen.Screen`.

A Fabric Loom `build.gradle` has been added. Build with Java 21 and network access using:

```bash
./gradlew clean build
```

The distributable mod JAR should be in `build/libs/velora-1.1.2.jar`. Use that Gradle-produced JAR, not manually packaged `.class` files. The build was not completed in this environment because the Gradle distribution host could not be resolved (`UnknownHostException: services.gradle.org`), so this source repair is not yet a verified compiled fix.
