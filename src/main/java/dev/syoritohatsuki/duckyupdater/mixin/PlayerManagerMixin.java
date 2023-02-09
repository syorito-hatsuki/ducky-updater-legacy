package dev.syoritohatsuki.duckyupdater.mixin;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.dto.ProjectVersion;
import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        AtomicInteger index = new AtomicInteger(1);
        DuckyUpdater.check(SharedConstants.getGameVersion().getName()).forEach(data -> {
            if (index.get() == 1) player.sendMessage(Text.literal("Updates available"));

            player.sendMessage(Text.literal(index.get() + ". ").append(updateText(data)).styled(style ->
                    style.withHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.literal(data.getLeft().changelog)
                            )
                    )
            ), false);

            index.getAndIncrement();
        });
    }

    private MutableText updateText(Pair<ProjectVersion, Pair<String, String>> projectVersion) {

        final String match = match(
                projectVersion.getRight().getRight().toCharArray(),
                projectVersion.getLeft().version_number.toCharArray()
        );

        final String oldVersion = projectVersion.getRight().getRight().replace(match, "");
        final String newVersion = projectVersion.getLeft().version_number.replace(match, "");

        return MutableText
                .of(TextContent.EMPTY)
                .append(Text.literal(projectVersion.getRight().getLeft()))
                .append(Text.literal(" [").formatted(Formatting.GRAY))
                .append(Text.literal(match).formatted(Formatting.GRAY))
                .append(Text.literal(oldVersion).formatted(Formatting.RED))
                .append(Text.literal(" -> ").formatted(Formatting.GRAY))
                .append(Text.literal(match).formatted(Formatting.GRAY))
                .append(Text.literal(newVersion).formatted(Formatting.GREEN))
                .append(Text.literal("]").formatted(Formatting.GRAY));
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