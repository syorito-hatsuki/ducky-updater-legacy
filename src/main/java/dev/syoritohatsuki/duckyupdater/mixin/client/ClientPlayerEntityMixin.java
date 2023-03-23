package dev.syoritohatsuki.duckyupdater.mixin.client;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.dto.UpdateData;
import net.minecraft.SharedConstants;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
        DuckyUpdater.check(SharedConstants.getGameVersion().getName()).forEach(updateData -> {
            if (firstLine.get()) {
                sendMessage(Text.literal("Updates available").styled(style ->
                        style.withBold(true).withColor(Formatting.YELLOW)));
                firstLine.set(false);
            }

            sendMessage(Text.literal(" - ").append(updateText(updateData)).styled(style ->
                    style.withHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.literal(updateData.projectVersion().changelog)
                            )
                    ).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateData.projectVersion().files[0].url))
            ));
        });
    }

    private MutableText updateText(UpdateData updateData) {

        final String match = DuckyUpdater.match(
                updateData.localVersion().toCharArray(),
                updateData.projectVersion().version_number.toCharArray()
        );

        final String oldVersion = updateData.localVersion().replace(match, "");
        final String newVersion = updateData.projectVersion().version_number.replace(match, "");

        return Text.literal(updateData.name())
                .append(Text.literal(" [").formatted(Formatting.DARK_GRAY))
                .append(Text.literal(match).formatted(Formatting.GRAY))
                .append(Text.literal(oldVersion).formatted(Formatting.RED))
                .append(Text.literal(" -> ").formatted(Formatting.DARK_GRAY))
                .append(Text.literal(match).formatted(Formatting.GRAY))
                .append(Text.literal(newVersion).formatted(Formatting.GREEN))
                .append(Text.literal("]").formatted(Formatting.DARK_GRAY));
    }
}