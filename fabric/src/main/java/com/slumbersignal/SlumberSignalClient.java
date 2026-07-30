package com.slumbersignal;

import com.slumbersignal.hud.SlumberToast;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

/**
 * Client entrypoint. Wires the night watcher to the client tick and the animated toast to the HUD.
 */
public final class SlumberSignalClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// 1. tick: decide when the player may sleep
		ClientTickEvents.END_CLIENT_TICK.register(SleepWatcher::onEndClientTick);

		// 2. reset rules: dimension swap, (re)join and disconnect must all re-arm the notifier
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> SleepWatcher.reset("level change"));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> SleepWatcher.reset("join"));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SleepWatcher.reset("disconnect"));

		// 3. draw the animated notification on top of the whole HUD
		HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, SlumberSignal.id("sleep_toast"),
				(extractor, deltaTracker) -> SlumberToast.render(extractor, deltaTracker));

		SlumberSignal.LOGGER.info("{} ready - client side only, no config files.", SlumberSignal.MOD_NAME);
	}
}
