package dev.syoritohatsuki.duckyupdater.mixin.client;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.StringUtil;
import dev.syoritohatsuki.duckyupdater.dto.UpdateData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Shadow
    public abstract void sendMessage(Text message);

    @Inject(method = "init", at = @At("TAIL"))
    public void mixedInit(CallbackInfo ci) {
        AtomicBoolean firstLine = new AtomicBoolean(true);
        DuckyUpdater.requestUpdates().forEach(((pair, updateData) -> {
            if (firstLine.get()) {
                sendMessage(Text.literal("Updates available").styled(style ->
                        style.withBold(true).withColor(Formatting.YELLOW)));
                firstLine.set(false);
            }
            sendMessage(Text.literal(" - ").append(updateText(pair, updateData)).styled(style ->
                    style.withHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.literal(updateData.changelog())
                            )
                    ).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateData.fileUrl()))
            ));
        }));
    }

    @Unique
    private MutableText updateText(Pair<String, String> pair, UpdateData updateData) {
        final String common = StringUtil.match(pair.getRight().toCharArray(), updateData.remoteVersion().toCharArray());

        final String oldVersion = pair.getRight().replace(common, "");
        final String newVersion = updateData.remoteVersion().replace(common, "");

        return Text.literal(pair.getLeft())
                .append(Text.literal(" [").formatted(Formatting.DARK_GRAY))
                .append(Text.literal(common).formatted(Formatting.GRAY))
                .append(Text.literal(oldVersion).formatted(Formatting.RED))
                .append(Text.literal(" -> ").formatted(Formatting.DARK_GRAY))
                .append(Text.literal(common).formatted(Formatting.GRAY))
                .append(Text.literal(newVersion).formatted(Formatting.GREEN))
                .append(Text.literal("]").formatted(Formatting.DARK_GRAY));
    }
}