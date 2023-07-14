package dev.syoritohatsuki.duckyupdater

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.logging.LogUtils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import org.slf4j.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object DuckyUpdater {

    val logger: Logger = LogUtils.getLogger()

    fun requestUpdates(): Map<Pair<String, String>, UpdateData> =
        mutableMapOf<Pair<String, String>, UpdateData>().apply {
            FabricLoader.getInstance().allMods.forEach { container ->
                if (container.metadata.getCustomValue("duckyupdater") == null) return@forEach
                try {
                    (Gson().fromJson(
                        HttpClient.newHttpClient().send(
                            HttpRequest.newBuilder()
                                .uri(
                                    URI.create(
                                        buildString {
                                            append("https://api.modrinth.com/v2/project/")
                                            append(
                                                (container.metadata.getCustomValue("duckyupdater")
                                                    ?: return@forEach).asObject.get("modrinthId").asString
                                            )
                                            append("/version?loaders=[%22fabric%22]&game_versions=[%22")
                                            append(SharedConstants.getGameVersion().name)
                                            append("%22]&featured=")
                                            append(
                                                container.metadata.getCustomValue("duckyupdater").asObject.get("featured")?.asBoolean
                                                    ?: false
                                            )
                                        }
                                    )
                                )
                                .setHeader(
                                    "User-Agent",
                                    "syorito-hatsuki/ducky-updater-lib/${
                                        FabricLoader.getInstance().getModContainer("ducky-updater-lib")
                                            .orElseThrow().metadata.version.friendlyString
                                    } (syorito-hatsuki.dev)"
                                )
                                .GET()
                                .build(), HttpResponse.BodyHandlers.ofString()
                        ).body(), JsonElement::class.java
                    ).asJsonArray[0]?.asJsonObject ?: return@forEach).let { json ->

                        if (json.get("version_number").asString == container.metadata.version.friendlyString) return@forEach

                        UpdateData(
                            json.get("version_number").asString,
                            json.get("changelog").asString,
                            json.get("version_type").asString,
                            json.get("files").asJsonArray[0].asJsonObject.get("url").asString
                        ).also {
                            if (it.type == (container.metadata.getCustomValue("duckyupdater").asObject.get("type")?.asString
                                    ?: "release")
                            ) this[Pair(
                                container.metadata.name,
                                container.metadata.version.friendlyString
                            )] = it
                        }
                    }
                } catch (e: Exception) {
                    logger.warn("Can't get update for ${container.metadata.id}", e)
                }
            }
        }

    fun match(oldVersion: CharArray, newVersion: CharArray): String {
        val result = StringBuilder()
        try {
            var index = 0
            while (oldVersion[index] == newVersion[index]) {
                result.append(oldVersion[index])
                index++
            }
        } catch (_: ArrayIndexOutOfBoundsException) {
        }
        return result.toString()
    }

    class UpdateData(
        val remoteVersion: String,
        val changelog: String,
        val type: String,
        val fileUrl: String
    )
}