package dev.syoritohatsuki.duckyupdater.mixin;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
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

        String BOLD = "\u001B[1m";
        String BRIGHT_GRAY = "\u001B[37m";
        String BRIGHT_GREEN = "\u001B[92m";
        String BRIGHT_RED = "\u001B[91m";
        String GRAY = "\u001B[90m";
        String RESET = "\u001B[0m";
        String YELLOW = "\u001B[33m";

        AtomicBoolean firstLine = new AtomicBoolean(true);
        Executors.newSingleThreadExecutor().submit(() -> DuckyUpdater.INSTANCE.requestUpdates().forEach((ducky, updateData) -> {

            if (firstLine.get()) {
                DuckyUpdater.INSTANCE.getLogger().info("");
                DuckyUpdater.INSTANCE.getLogger().info(BOLD + YELLOW + "Updates available" + RESET);
                firstLine.set(false);
            }

            var oldVersion = ducky.getSecond();
            var newVersion = updateData.getRemoteVersion();
            var commonPrefix = DuckyUpdater.INSTANCE.match(oldVersion.toCharArray(), newVersion.toCharArray());

            DuckyUpdater.INSTANCE.getLogger().info("\t- {} " + GRAY + "[" + BRIGHT_GRAY + "{}" + BRIGHT_RED + "{}" + GRAY + " -> " + BRIGHT_GRAY + "{}" + BRIGHT_GREEN + "{}" + GRAY + "]" + RESET, ducky.getFirst(), commonPrefix, oldVersion, commonPrefix, newVersion);

            if (!firstLine.get()) DuckyUpdater.INSTANCE.getLogger().info("");
        }));
    }
}