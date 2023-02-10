package dev.syoritohatsuki.duckyupdater.dto.modrinth;

public record ProjectVersion(
        String changelog,
        String version_type,
        String version_number,
        Files[] files
) {

}