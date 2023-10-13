package dev.syoritohatsuki.duckyupdater;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;

public final class StringUtil {

    public static String buildUrl(ModContainer modContainer) {
        var featured = false;

        var duckyUpdaterObject = modContainer.getMetadata().getCustomValue("duckyupdater").getAsObject();
        if (duckyUpdaterObject == null) return null;

        var modrinthId = duckyUpdaterObject.get("modrinthId");
        if (modrinthId == null) return null;

        var featuredObject = duckyUpdaterObject.get("featured");
        if (featuredObject != null) featured = featuredObject.getAsBoolean();

        return "https://api.modrinth.com/v2/project/" + modrinthId.getAsString() + "/version?loaders=[%22fabric%22]&game_versions=[%22" + SharedConstants.getGameVersion().getName() + "%22]&featured=" + featured;
    }

    public static String match(char[] oldVersion, char[] newVersion) {
        var result = new StringBuilder();
        try {
            var index = 0;
            while (oldVersion[index] == newVersion[index]) {
                result.append(oldVersion[index]);
                index++;
            }
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        return result.toString();
    }

    public static String userAgent(ModContainer modContainer) {
        return "syorito-hatsuki/ducky-updater-lib/ + " + modContainer.getMetadata().getVersion().getFriendlyString() + "(syorito - hatsuki.dev)";
    }
}