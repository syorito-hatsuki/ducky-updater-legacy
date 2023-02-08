package dev.syoritohatsuki.duckyupdater;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import dev.syoritohatsuki.duckyupdater.dto.MetaData;
import dev.syoritohatsuki.duckyupdater.dto.ProjectVersion;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;

public class DuckyUpdater {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String URL = "https://api.modrinth.com/v2/";

    private static final HashSet<MetaData> MODRINTH_ID_LIST = new HashSet<>();

    static void checkForUpdate(String modrinthId, String modId) {
        checkForUpdate(modrinthId, modId, true);
    }

    static void checkForUpdate(String modrinthId, String modId, Boolean onlyFeatured) {
        checkForUpdate(modrinthId, modId, "release", onlyFeatured);
    }

    static void checkForUpdate(String modrinthId, String modId, String type, Boolean onlyFeatured) {
        MODRINTH_ID_LIST.add(new MetaData(modrinthId, modId, type, onlyFeatured));
    }

    /**
     * <b>ONLY IN MIXIN USE!!!<b/>
     *
     * @param minecraftVersion required for request data only for current version
     * @return Set of all projects that have update available
     */
    public static HashSet<Pair<ProjectVersion, String>> check(String minecraftVersion) {
        HashSet<Pair<ProjectVersion, String>> projectVersionsSet = new HashSet<>();
        LOGGER.info("MC: " + minecraftVersion);
        MODRINTH_ID_LIST.forEach(metaData -> {
            try {
                ProjectVersion[] projectVersions = new Gson().fromJson(
                        HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                                .uri(URI.create(URL + "project/" + metaData.modrinthId() +
                                        "/version?loaders=[%22fabric%22]" +
                                        "&game_versions=[%22" + minecraftVersion + "%22]" +
                                        "&featured=" + metaData.onlyFeatured()))
                                .GET()
                                .build(), HttpResponse.BodyHandlers.ofString()).body(), ProjectVersion[].class);

                if (projectVersions[0].version_type.equals(metaData.type())) {

                    var modVersion = getModVersion(metaData.modId());

                    if (!projectVersions[0].version_number.contains(modVersion))
                        projectVersionsSet.add(new Pair<>(projectVersions[0], modVersion));

                }
            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
            }
        });
        return projectVersionsSet;
    }

    private static String getModVersion(String modId) {
        return FabricLoader.getInstance()
                .getModContainer(modId)
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }
}