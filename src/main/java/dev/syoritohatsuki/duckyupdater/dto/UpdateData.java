package dev.syoritohatsuki.duckyupdater.dto;

import dev.syoritohatsuki.duckyupdater.dto.modrinth.ProjectVersion;

public record UpdateData(
        String name,
        String localVersion,
        ProjectVersion projectVersion
) {
}
