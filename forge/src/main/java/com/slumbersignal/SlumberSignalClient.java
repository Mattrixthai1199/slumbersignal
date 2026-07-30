package com.slumbersignal;

import com.slumbersignal.hud.SlumberToast;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge entrypoint. Wires the night watcher to the client tick and the animated toast to the HUD.
 *
 * <p>The mod is declared {@code clientSideOnly} in {@code mods.toml}, so this class is only ever
 * constructed on a physical client.</p>
 */
@Mod(SlumberSignal.MOD_ID)
public final class SlumberSignalClient {
	public SlumberSignalClient(FMLJavaModLoadingContext context) {
		// 1. tick: decide when the player may sleep
		TickEvent.ClientTickEvent.Post.BUS.addListener(event -> SleepWatcher.onEndClientTick(Minecraft.getInstance()));

		// 2. reset rules: (re)join, disconnect and world unload must all re-arm the notifier.
		//    A plain dimension swap keeps the same connection, so SleepWatcher detects that itself.
		ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(event -> SleepWatcher.reset("join"));
		ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> SleepWatcher.reset("disconnect"));
		LevelEvent.Unload.BUS.addListener(SlumberSignalClient::onLevelUnload);

		// 3. draw the animated notification last, i.e. on top of the whole vanilla HUD
		AddGuiOverlayLayersEvent.BUS.addListener(event ->
				event.getLayeredDraw().add(SlumberSignal.id("sleep_toast"), SlumberToast::render));

		SlumberSignal.LOGGER.info("{} ready - client side only, no config files.", SlumberSignal.MOD_NAME);
	}

	/** Only the client copy of a level matters here; the integrated server fires this too. */
	private static void onLevelUnload(LevelEvent.Unload event) {
		LevelAccessor level = event.getLevel();

		if (level.isClientSide()) {
			SleepWatcher.reset("level unload");
		}
	}
}
