package dev.syoritohatsuki.duckyupdater;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import dev.syoritohatsuki.duckyupdater.dto.ProjectVersion;
import dev.syoritohatsuki.duckyupdater.dto.ProjectVersions;
import net.minecraft.util.Pair;
import org.slf4j.Logger;
import oshi.util.tuples.Triplet;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

public class DuckyUpdater {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String URL = "https://staging-api.modrinth.com/v2/";

    static Set<Triplet<String, String, Boolean>> MODRINTH_ID_LIST = Set.of();
    static Set<ProjectVersion> UPDATE_AVAILABLE = Set.of();

    static void checkForUpdate(String modrinthId) {
        checkForUpdate(modrinthId, true);
    }

    static void checkForUpdate(String modrinthId, Boolean onlyFeatured) {
        checkForUpdate(modrinthId, "release", onlyFeatured);
    }

    static void checkForUpdate(String modrinthId, String type, Boolean onlyFeatured) {
        MODRINTH_ID_LIST.add(new Triplet<>(modrinthId, type, onlyFeatured));
    }

    private static void check(String minecraftVersion) {
        MODRINTH_ID_LIST.forEach(params -> {
            ProjectVersions projectVersions;
            try {
                projectVersions = new Gson().fromJson(HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                        .uri(URI.create(URL + "project/" + params.getA() + "/version?loaders=[\"fabric\"]&game_versions=[\"" + minecraftVersion + "\"]&featured" + params.getC()))
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString()).body(), ProjectVersions.class);

            } catch (Exception exception) {
                LOGGER.error(exception.getLocalizedMessage());
                return;
            }
            if (projectVersions.getVersionList().get(0).version_type.equals(params.getB())) ;
        });
    }
}
