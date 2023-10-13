package dev.syoritohatsuki.duckyupdater.mixin;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.StringUtil;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {

    @Inject(method = "setupServer", at = @At("TAIL"))
    protected void runServerReturn(CallbackInfoReturnable<Boolean> cir) {
        Executors.newSingleThreadExecutor().execute(() -> {

            var firstLine = new AtomicBoolean(true);

            DuckyUpdater.requestUpdates().forEach((ducky, updateData) -> {

                String BOLD = "\u001B[1m";
                String BRIGHT_GRAY = "\u001B[37m";
                String BRIGHT_GREEN = "\u001B[92m";
                String BRIGHT_RED = "\u001B[91m";
                String GRAY = "\u001B[90m";
                String RESET = "\u001B[0m";
                String YELLOW = "\u001B[33m";

                if (firstLine.get()) {
                    DuckyUpdater.logger.info("");
                    DuckyUpdater.logger.info(BOLD + YELLOW + "Updates available" + RESET);
                    firstLine.set(false);
                }

                var oldVersion = ducky.getRight();
                var newVersion = updateData.remoteVersion();
                var common = StringUtil.match(oldVersion.toCharArray(), newVersion.toCharArray());

                DuckyUpdater.logger.info("\t- {} " + GRAY + "[" + BRIGHT_GRAY + "{}" + BRIGHT_RED + "{}" + GRAY + " -> " + BRIGHT_GRAY + "{}" + BRIGHT_GREEN + "{}" + GRAY + "]" + RESET, ducky.getLeft(), common, oldVersion.replace(common, ""), common, newVersion.replace(common, ""));

            });

            if (!firstLine.get()) DuckyUpdater.logger.info("");

        });
    }
}