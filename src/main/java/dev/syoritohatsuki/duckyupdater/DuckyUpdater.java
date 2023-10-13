package dev.syoritohatsuki.duckyupdater;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import dev.syoritohatsuki.duckyupdater.dto.UpdateData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class DuckyUpdater {

    public static final Logger logger = LogUtils.getLogger();

    public static Map<Pair<String, String>, UpdateData> requestUpdates() {

        final var updateMap = new HashMap<Pair<String, String>, UpdateData>();

        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            if (modContainer.getMetadata().getCustomValue("duckyupdater") == null) return;
            try {
                var url = StringUtil.buildUrl(modContainer);
                if (url == null) return;

                var json = new Gson().fromJson(
                        HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .setHeader("User-Agent", StringUtil.userAgent(modContainer))
                                .GET()
                                .build(), HttpResponse.BodyHandlers.ofString()).body(), JsonElement.class
                ).getAsJsonArray().get(0).getAsJsonObject();

                var remoteVersion = json.get("version_number").getAsString();
                if (remoteVersion.equals(modContainer.getMetadata().getVersion().getFriendlyString())) return;

                var updateData = new UpdateData(remoteVersion, json.get("changelog").getAsString(), json.get("version_type").getAsString(), json.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString());

                var type = "release";
                var typeObject = modContainer.getMetadata().getCustomValue("duckyupdater").getAsObject().get("type");
                if (typeObject != null) type = typeObject.getAsString();

                if (!updateData.type().equals(type)) return;

                updateMap.put(new Pair<>(modContainer.getMetadata().getName(), modContainer.getMetadata().getVersion().getFriendlyString()), updateData);
            } catch (Exception e) {
                logger.warn("Can't get update for " + modContainer.getMetadata().getId(), e);
            }
        });
        return updateMap;
    }
}
