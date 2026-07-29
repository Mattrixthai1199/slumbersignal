package com.slumbersignal;

import com.slumbersignal.hud.SlumberToast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;

/**
 * Watches the Overworld clock and fires the notification on the exact tick sleeping becomes
 * possible, at most once per night.
 *
 * <p>All state is static and lives only in memory - nothing is ever written to disk.</p>
 */
public final class SleepWatcher {
	/** Length of one Minecraft day in ticks. */
	private static final long DAY_TICKS = 24000L;

	private static boolean notifiedThisNight;
	private static long lastDayIndex = Long.MIN_VALUE;
	private static ResourceKey<Level> lastDimension;

	private SleepWatcher() {
	}

	public static void onEndClientTick(Minecraft client) {
		SlumberToast.tick();

		LocalPlayer player = client.player;
		ClientLevel level = client.level;
		if (player == null || level == null) {
			// title screen / world unloaded - reset once, not every tick
			if (lastDimension != null || notifiedThisNight) {
				reset("no world");
			}

			return;
		}

		ResourceKey<Level> dimension = level.dimension();

		// (5) dimension change -> forget everything so the next night notifies again
		if (!dimension.equals(lastDimension)) {
			lastDimension = dimension;
			notifiedThisNight = false;
			lastDayIndex = Long.MIN_VALUE;
		}

		// (2) Overworld only
		if (!Level.OVERWORLD.equals(dimension)) {
			notifiedThisNight = false;
			return;
		}

		// a new in-game day always re-arms the notifier
		long dayIndex = level.getOverworldClockTime() / DAY_TICKS;
		if (dayIndex != lastDayIndex) {
			lastDayIndex = dayIndex;
			notifiedThisNight = false;
		}

		// (1) is it dark enough to use a bed right now?
		boolean canSleep = canSleepNow(level, player);

		if (!canSleep) {
			// daytime again -> arm for the coming night
			notifiedThisNight = false;
			return;
		}

		// (3) + (4) exactly one notification per night
		if (!notifiedThisNight) {
			notifiedThisNight = true;
			SlumberToast.show();
			player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.55F, 1.25F);
			player.playSound(SoundEvents.BEACON_ACTIVATE, 0.18F, 1.9F);
			SlumberSignal.LOGGER.debug("night {} - {}", dayIndex, SlumberSignal.MESSAGE);
		}
	}

	/**
	 * Uses the very same rule the server uses in {@code ServerPlayer#startSleepInBed}: the
	 * {@code BED_RULE} environment attribute at the player's position. Falls back to the raw
	 * darkness check if the attribute is not available on the client for some reason.
	 */
	private static boolean canSleepNow(ClientLevel level, LocalPlayer player) {
		try {
			BedRule rule = level.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, player.blockPosition());

			if (rule != null) {
				return rule.canSleep(level);
			}
		} catch (Throwable ignored) {
			// never let a rendering-thread exception escape into the game loop
		}

		return level.isDarkOutside();
	}

	/** (5) Called on join, disconnect and dimension change. */
	public static void reset(String reason) {
		notifiedThisNight = false;
		lastDayIndex = Long.MIN_VALUE;
		lastDimension = null;
		SlumberToast.hide();
		SlumberSignal.LOGGER.debug("state reset ({})", reason);
	}
}
