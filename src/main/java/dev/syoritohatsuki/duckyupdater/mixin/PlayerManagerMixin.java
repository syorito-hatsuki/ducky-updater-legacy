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
                player.sendMessage(Text.literal("Updates available").styled(style ->
                        style.withBold(true).withColor(Formatting.YELLOW)));
                firstLine.set(false);
            }

            player.sendMessage(Text.literal(" - ").append(updateText(updateData)).styled(style ->
                    style.withHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.literal(updateData.projectVersion().files()[0].url())
                            )
                    ).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ""))
            ), false);
        });
    }

    private MutableText updateText(UpdateData updateData) {

        final String match = match(
                updateData.localVersion().toCharArray(),
                updateData.projectVersion().version_number().toCharArray()
        );

        final String oldVersion = updateData.localVersion().replace(match, "");
        final String newVersion = updateData.projectVersion().version_number().replace(match, "");

        return MutableText
                .of(TextContent.EMPTY)
                .append(Text.literal(updateData.name()))
                .append(Text.literal(" [").formatted(Formatting.DARK_GRAY))
                .append(Text.literal(match).formatted(Formatting.GRAY))
                .append(Text.literal(oldVersion).formatted(Formatting.RED))
                .append(Text.literal(" -> ").formatted(Formatting.DARK_GRAY))
                .append(Text.literal(match).formatted(Formatting.GRAY))
                .append(Text.literal(newVersion).formatted(Formatting.GREEN))
                .append(Text.literal("]").formatted(Formatting.DARK_GRAY));
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