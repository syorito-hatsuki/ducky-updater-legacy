package dev.syoritohatsuki.duckyupdater.mixin;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.dto.UpdateData;
import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        AtomicBoolean firstLine = new AtomicBoolean(true);
        DuckyUpdater.check(SharedConstants.getGameVersion().getName()).forEach(updateData -> {
            if (firstLine.get()) {
                player.sendMessage(new LiteralText("Updates available").styled(style ->
                        style.withBold(true).withColor(Formatting.YELLOW)), false);
                firstLine.set(false);
            }

            player.sendMessage(new LiteralText(" - ").append(updateText(updateData)).styled(style ->
                    style.withHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new LiteralText(updateData.projectVersion().changelog)
                            )
                    ).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateData.projectVersion().files[0].url))
            ), false);
        });
    }

    private MutableText updateText(UpdateData updateData) {

        final String match = match(
                updateData.localVersion().toCharArray(),
                updateData.projectVersion().version_number.toCharArray()
        );

        final String oldVersion = updateData.localVersion().replace(match, "");
        final String newVersion = updateData.projectVersion().version_number.replace(match, "");

        return new LiteralText(updateData.name())
                .append(new LiteralText(" [").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(match).formatted(Formatting.GRAY))
                .append(new LiteralText(oldVersion).formatted(Formatting.RED))
                .append(new LiteralText(" -> ").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(match).formatted(Formatting.GRAY))
                .append(new LiteralText(newVersion).formatted(Formatting.GREEN))
                .append(new LiteralText("]").formatted(Formatting.DARK_GRAY));
    }

    private String match(char[] oldVersion, char[] newVersion) {
        int index = 0;
        StringBuilder result = new StringBuilder();
        while (oldVersion[index] == newVersion[index]) {
            result.append(oldVersion[index]);
            index++;
        }
        return result.toString();
    }
}