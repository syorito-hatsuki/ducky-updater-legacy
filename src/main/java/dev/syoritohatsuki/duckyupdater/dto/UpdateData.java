package dev.syoritohatsuki.duckyupdater.dto;

public record UpdateData(String remoteVersion, String changelog, String type, String fileUrl) {
}
