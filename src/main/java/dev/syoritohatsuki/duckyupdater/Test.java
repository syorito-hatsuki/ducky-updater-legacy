package dev.syoritohatsuki.duckyupdater;

import net.fabricmc.api.ModInitializer;

public class Test implements ModInitializer {
    @Override
    public void onInitialize() {
        DuckyUpdater.checkForUpdate("Ha28R6CL", "fabric-language-kotlin");
    }
}
