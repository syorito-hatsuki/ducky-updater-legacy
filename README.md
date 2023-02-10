# Ducky Updater
<img src="https://i.imgur.com/iaETp3c.png" alt="" width="200" > <img src="https://i.imgur.com/Ol1Tcf8.png" alt="" width="200" >

## Description
Simple library for checking mod updates from modrinth

## Adding the dependency
```gradle
repositories {
    maven {
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    // Option 1: Include Ducky Updater to project for it available within your own jar (additional ~20kb)
    include(modImplementation("maven.modrinth", "ducky-updater", "<version>"))
    
    // Option 2: Depend on Ducky Updater, but require that users install it manually
    modImplementation("maven.modrinth", "ducky-updater", "<version>")
}
```

```json5
"depends": {
"fabricloader": "*",
...
//    Also add dependency in your fabric.mod.json 
"ducky-updater": "*"
},
```

## Usage

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
