# Ducky Updater Lib

<img src="https://i.imgur.com/iaETp3c.png" alt="" width="200" >

## Description

Simple library for checking mod updates from Modrinth

## Adding the dependency

> build.gradle.kts

```gradle
repositories {
    maven {
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    // Option 1: Include Ducky Updater to project for it available within your own jar (additional ~17kb)
    include(modImplementation("maven.modrinth", "ducky-updater-lib", "<version>"))
    
    // Option 2: Depend on Ducky Updater, but require that users install it manually
    modImplementation("maven.modrinth", "ducky-updater-lib", "<version>")
}
```

> fabric.mod.json

```json5
{
  "depends": {
    "fabricloader": "*",
    ...
    // Also add dependency in your fabric.mod.json 
    "ducky-updater": "*"
  }
}
```

## Usage

> fabric.mod.json

```json5
{
  "custom": {
    ...
    "duckyupdater": {
      //Mod modrinth ID from project page
      "modrinthId": "mWxGwd3F",
      // Optional (release, beta, alpha)
      // Default: release
      "type": "release",
      // Optional (true, false)
      // Default false
      "featured": false
    }
  },
}
```

<details>
    <summary>Before 2023.7.1</summary>

```java
public class ModName implements ModInitializer {
    @Override
    public void onInitialize() {
        DuckyUpdater.checkForUpdate(
                "modrinthId",
                "modId",
                "alpha",        // Optional! Default release 
                true            // Optional! Default true
        );
    }
}
```

</details>
