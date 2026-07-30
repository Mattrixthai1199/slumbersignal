package com.slumbersignal.hud;

import com.slumbersignal.SlumberSignal;
import net.minecraft.resources.Identifier;

/** Every image shipped with the mod. */
public final class SsTextures {
	private static Identifier gui(String name) {
		return SlumberSignal.id("textures/gui/" + name + ".png");
	}

	private static Identifier art(String name) {
		return SlumberSignal.id("textures/art/" + name + ".png");
	}

	/** 512x162 - notification plate. */
	public static final Identifier PANEL = gui("toast_panel");
	/** 1024x256 - animated wordmark. */
	public static final Identifier WORDMARK = gui("logo_wordmark");
	/** 128x128 icons. */
	public static final Identifier MOON = gui("moon");
	public static final Identifier BED = gui("bed");
	public static final Identifier ZZZ = gui("zzz");
	public static final Identifier FLOURISH = gui("corner_flourish");
	/** 256x256, 2x2 sprite sheet of 128x128 frames. */
	public static final Identifier SPARKLES = gui("sparkles");
	/** 256x256 additive-looking blobs. */
	public static final Identifier GLOW = gui("glow");
	public static final Identifier RING = gui("ring_pulse");
	/** 512x128 shine streak. */
	public static final Identifier SHINE = gui("shine_sweep");
	/** 512x512 drifting stars. */
	public static final Identifier STARS = gui("star_field");
	/** 1024x1024 screen vignette. */
	public static final Identifier VIGNETTE = gui("vignette");
	/** 512x256 parallax art inside the panel. */
	public static final Identifier SPLASH = gui("splash_strip");
	/** Cover art / banner, also used as a wide backdrop flash. */
	public static final Identifier COVER = art("cover");
	public static final Identifier BANNER = art("banner_wide");
	public static final Identifier ICON_SMALL = art("icon_small");

	private SsTextures() {
	}
}
