package dev.syoritohatsuki.duckyupdater;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import dev.syoritohatsuki.duckyupdater.dto.MetaData;
import dev.syoritohatsuki.duckyupdater.dto.UpdateData;
import dev.syoritohatsuki.duckyupdater.dto.modrinth.ProjectVersion;
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

    /**
     * @param modrinthId you can get it on Modrinth project page
     * @param modId      your mod id from fabric.mod.json
     */
    public static void checkForUpdate(String modrinthId, String modId) {
        checkForUpdate(modrinthId, modId, true);
    }

    /**
     * @param modrinthId   you can get it on Modrinth project page
     * @param modId        your mod id from fabric.mod.json
     * @param onlyFeatured I don't know what say about it :)
     */
    public static void checkForUpdate(String modrinthId, String modId, Boolean onlyFeatured) {
        checkForUpdate(modrinthId, modId, "release", onlyFeatured);
    }

    /**
     * @param modrinthId   you can get it on Modrinth project page
     * @param modId        your mod id from fabric.mod.json
     * @param type         channel type [release, beta, alpha]
     * @param onlyFeatured I don't know what say about it :)
     */
    public static void checkForUpdate(String modrinthId, String modId, String type, Boolean onlyFeatured) {
        MODRINTH_ID_LIST.add(new MetaData(modrinthId, modId, type, onlyFeatured));
    }

    /**
     * <b>ONLY IN MIXIN USE!!!<b/>
     *
     * @param minecraftVersion required for request data only for current version
     * @return Set of all projects that have update available
     */
    public static HashSet<UpdateData> check(String minecraftVersion) {
        HashSet<UpdateData> projectVersionsSet = new HashSet<>();

        MODRINTH_ID_LIST.forEach(metaData -> {
            var url = URI.create(URL + "project/" + metaData.modrinthId() +
                    "/version?loaders=[%22fabric%22]" +
                    "&game_versions=[%22" + minecraftVersion + "%22]" +
                    "&featured=" + metaData.onlyFeatured());

            LOGGER.info(url.toString());

            try {
                ProjectVersion[] projectVersions = new Gson().fromJson(
                        HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                                .uri(url)
                                .setHeader("User-Agent", "syorito-hatsuki/ducky-updater/" + getModVersion() + " (syorito-hatsuki.dev)")
                                .GET()
                                .build(), HttpResponse.BodyHandlers.ofString()).body(), ProjectVersion[].class);

                if (projectVersions[0].version_type.equals(metaData.type())) {

                    var modNameAndVersion = getModNameAndVersion(metaData.modId());

                    if (!projectVersions[0].version_number.contains(modNameAndVersion.getRight()))
                        projectVersionsSet.add(
                                new UpdateData(
                                        modNameAndVersion.getLeft(),
                                        modNameAndVersion.getRight(),
                                        projectVersions[0]
                                )
                        );

                }
            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
            }
        });
        return projectVersionsSet;
    }

    private static Pair<String, String> getModNameAndVersion(String modId) {

        var metadata = FabricLoader.getInstance()
                .getModContainer(modId)
                .orElseThrow()
                .getMetadata();

        return new Pair<>(metadata.getName(), metadata.getVersion().getFriendlyString());
    }

    private static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer("ducky-updater")
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }
}