package dev.syoritohatsuki.duckyupdater.mixin;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "loadWorld", at = @At("TAIL"))
    protected void runServerReturn(CallbackInfo ci) {
        AtomicBoolean firstLine = new AtomicBoolean(true);
        DuckyUpdater.check(SharedConstants.getGameVersion().getName()).forEach(updateData -> {
            if (firstLine.get()) {
                LOGGER.info("");
                LOGGER.info("\u001B[1m\u001B[33mUpdates available\u001B[0m");
                firstLine.set(false);
            }

            final String match = DuckyUpdater.match(
                    updateData.localVersion().toCharArray(),
                    updateData.projectVersion().version_number.toCharArray()
            );

            final String oldVersion = updateData.localVersion().replace(match, "");
            final String newVersion = updateData.projectVersion().version_number.replace(match, "");

            LOGGER.info("\t- {} \u001B[90m[\u001B[37m{}\u001B[91m{}\u001B[90m -> \u001B[37m{}\u001B[92m{}\u001B[90m]\u001B[0m",
                    updateData.name(), match, oldVersion, match, newVersion);
        });
        if (!firstLine.get()) LOGGER.info("");
    }
}