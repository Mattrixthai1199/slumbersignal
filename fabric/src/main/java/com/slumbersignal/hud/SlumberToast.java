package com.slumbersignal.hud;

import com.slumbersignal.SlumberSignal;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * The animated notification.
 *
 * <p>Everything is driven by a single tick counter, so the whole thing is one big timeline:
 * a soft bloom, the plate itself with a parallax backdrop and a shine sweep, four popping corner
 * ornaments, a shining wordmark, a breathing moon, a hopping bed with rising Zzz, orbiting
 * sparkles, typed text and a countdown bar.</p>
 *
 * <p>Compact panel, no full-screen flash, and the message always sits on one straight
 * baseline - the only thing that leaves the plate is the trio of soft shockwave rings.</p>
 */
public final class SlumberToast {
	/** Ticks the intro takes. */
	private static final float IN = 22F;
	/** Ticks the outro takes. */
	private static final float OUT = 30F;
	/** Total lifetime in ticks (20 ticks = 1 second). */
	private static final float TOTAL = 210F;

	/** Compact plate: roughly a third of a 427-wide GUI. */
	private static final float PANEL_W = 190F;
	private static final float PANEL_H = 60F;
	/** Resting distance between the top of the screen and the middle of the plate. */
	private static final float REST_Y = 40F;

	private static boolean active;
	private static float ticks;

	private SlumberToast() {
	}

	public static void show() {
		active = true;
		ticks = 0F;
	}

	public static void hide() {
		active = false;
		ticks = 0F;
	}

	public static boolean isActive() {
		return active;
	}

	public static void tick() {
		if (!active) {
			return;
		}

		ticks += 1F;

		if (ticks >= TOTAL) {
			hide();
		}
	}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		if (!active) {
			return;
		}

		Minecraft client = Minecraft.getInstance();

		if (client.player == null || client.level == null) {
			return;
		}

		float t = ticks + delta.getGameTimeDeltaPartialTick(false);
		float sw = g.guiWidth();
		float sh = g.guiHeight();

		// master envelope: fade in, hold, fade out
		float fadeIn = Gfx.easeOutCubic(t / IN);
		float fadeOut = 1F - Gfx.smoothstep(TOTAL - OUT, TOTAL, t);
		float alpha = Math.min(fadeIn, fadeOut);

		if (alpha <= 0.002F) {
			return;
		}

		float cx = sw / 2F;
		// slides down with an elastic overshoot, then drifts back up on the way out
		float enter = Gfx.easeOutElastic(t / IN);
		float exit = Gfx.easeInCubic((t - (TOTAL - OUT)) / OUT);
		float cy = Gfx.lerp(enter, -PANEL_H * 0.8F, REST_Y) - exit * 26F + Gfx.swing(t, 74F) * 1.6F;

		backdrop(g, t, alpha, sw, sh, cx, cy);
		rings(g, t, alpha, cx, cy);
		bloom(g, t, alpha, cx, cy);
		plate(g, t, alpha, cx, cy);
		ornaments(g, t, alpha, cx, cy);
		wordmark(g, t, alpha, cx, cy);
		icons(g, t, alpha, cx, cy);
		sparkles(g, t, alpha, cx, cy);
		message(g, client.font, t, alpha, cx, cy);
		countdown(g, t, alpha, cx, cy);
	}

	// ------------------------------------------------------------ layers

	/**
	 * A faint vignette plus two drifting star layers, both kept local to the panel.
	 *
	 * <p>No full-screen flash - nothing ever whites out the player's view.</p>
	 */
	private static void backdrop(GuiGraphicsExtractor g, float t, float alpha, float sw, float sh, float cx, float cy) {
		// calm breathing vignette
		float breathe = 0.20F + 0.04F * Gfx.wave(t, 96F);
		Gfx.sprite(g, SsTextures.VIGNETTE, cx, sh / 2F, sw, sh, 1024, 1024, 1F, 0F,
				Gfx.rgba(Gfx.WHITE, alpha * breathe));

		// two parallax star layers scrolling in opposite directions, each twinkling
		for (int layer = 0; layer < 2; layer++) {
			float speed = layer == 0 ? 0.22F : -0.13F;
			float size = layer == 0 ? 190F : 260F;
			float twinkle = 0.09F + 0.07F * Gfx.wave(t + layer * 31F, layer == 0 ? 37F : 53F);
			float drift = (t * speed) % size;

			for (int i = -1; i <= 2; i++) {
				Gfx.sprite(g, SsTextures.STARS, cx + drift + i * size, cy + (layer == 0 ? -4F : 6F),
						size, size * 0.5F, 512, 512, 1F, 0F, Gfx.rgba(Gfx.WHITE, alpha * twinkle));
			}
		}
	}

	/**
	 * Three shockwave rings expanding out of the plate, staggered so a new one leaves every
	 * 27 ticks. Sized to the compact plate and faded out quickly so they stay decorative.
	 */
	private static void rings(GuiGraphicsExtractor g, float t, float alpha, float cx, float cy) {
		for (int i = 0; i < 3; i++) {
			float local = t - 3F - i * 27F;

			if (local < 0F || local > 54F) {
				continue;
			}

			float p = local / 54F;
			float scale = Gfx.lerp(Gfx.easeOutCubic(p), 0.30F, 1.85F);
			float ringAlpha = (1F - p) * (1F - p) * 0.50F * alpha;
			Gfx.sprite(g, SsTextures.RING, cx, cy, 140F, 140F, 256, 256, scale, local * 0.008F,
					Gfx.rgba(Gfx.mixRgb(p, Gfx.WHITE, Gfx.LAVENDER), ringAlpha));
		}
	}

	/** Soft bloom sitting behind the plate - it breathes but never expands outwards. */
	private static void bloom(GuiGraphicsExtractor g, float t, float alpha, float cx, float cy) {
		float pulse = 1F + 0.05F * Gfx.wave(t, 44F);
		Gfx.sprite(g, SsTextures.GLOW, cx, cy, 236F, 176F, 256, 256, pulse, 0F,
				Gfx.rgba(Gfx.LAVENDER, alpha * (0.24F + 0.08F * Gfx.wave(t, 61F))));
	}

	/** Parallax art inside the plate, then the plate itself, slightly rocking. */
	private static void plate(GuiGraphicsExtractor g, float t, float alpha, float cx, float cy) {
		float pop = Gfx.lerp(Gfx.easeOutBack(t / IN), 0.78F, 1F);
		float breathe = 1F + 0.008F * Gfx.wave(t, 58F);
		float rock = Gfx.swing(t, 118F) * 0.009F;

		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.rotate(rock);
		pose.scale(pop * breathe, pop * breathe);

		// scrolling artwork behind the glass
		float scroll = (t * 0.45F) % 256F;
		Gfx.spriteRegion(g, SsTextures.SPLASH, 0F, 0F, PANEL_W - 8F, PANEL_H - 8F,
				scroll, 24F, 256, 128, 512, 256, 1F, 0F, Gfx.rgba(Gfx.WHITE, alpha * 0.30F));

		// the plate
		Gfx.sprite(g, SsTextures.PANEL, 0F, 0F, PANEL_W, PANEL_H, 512, 162, 1F, 0F, Gfx.rgba(Gfx.WHITE, alpha));

		// shine sweeping across the plate every 90 ticks
		float sweep = (t % 90F) / 34F;

		if (sweep <= 1F) {
			float sx = Gfx.lerp(sweep, -PANEL_W * 0.6F, PANEL_W * 0.6F);
			float sa = Math.min(sweep, 1F - sweep) * 2F * 0.45F * alpha;
			Gfx.sprite(g, SsTextures.SHINE, sx, 0F, 106F, PANEL_H * 0.95F, 512, 128, 1F, 0.35F,
					Gfx.rgba(Gfx.WHITE, sa));
		}

		pose.popMatrix();
	}

	/** Four corner ornaments popping in one after another and slowly counter-rotating. */
	private static void ornaments(GuiGraphicsExtractor g, float t, float alpha, float cx, float cy) {
		float halfW = PANEL_W / 2F - 9F;
		float halfH = PANEL_H / 2F - 9F;
		float[][] spots = {{-halfW, -halfH}, {halfW, -halfH}, {halfW, halfH}, {-halfW, halfH}};

		for (int i = 0; i < 4; i++) {
			float pop = Gfx.easeOutBack((t - 7F - i * 3.5F) / 15F);

			if (pop <= 0F) {
				continue;
			}

			float idle = 1F + 0.05F * Gfx.wave(t + i * 13F, 47F);
			float rotation = (float) (Math.PI / 2.0) * i + Gfx.swing(t + i * 9F, 150F) * 0.05F;
			Gfx.sprite(g, SsTextures.FLOURISH, cx + spots[i][0], cy + spots[i][1], 18F, 18F, 128, 128,
					Math.min(pop, 1.15F) * idle, rotation, Gfx.rgba(Gfx.WHITE, alpha * 0.95F));
		}
	}

	/** The logo above the plate, bobbing with its own little shine pass. */
	private static void wordmark(GuiGraphicsExtractor g, float t, float alpha, float cx, float cy) {
		float rise = Gfx.easeOutCubic((t - 4F) / 18F);

		if (rise <= 0F) {
			return;
		}

		float y = cy - PANEL_H / 2F - 12F + (1F - rise) * 8F + Gfx.swing(t, 88F) * 1.1F;
		float scale = Gfx.lerp(Gfx.easeOutBack((t - 4F) / 20F), 0.7F, 1F);
		Gfx.sprite(g, SsTextures.WORDMARK, cx, y, 104F, 26F, 1024, 256, scale, 0F,
				Gfx.rgba(Gfx.WHITE, alpha * rise));

		float sweep = ((t + 45F) % 110F) / 30F;

		if (sweep <= 1F) {
			Gfx.sprite(g, SsTextures.SHINE, cx + Gfx.lerp(sweep, -62F, 62F), y, 68F, 22F, 512, 128, 1F, 0F,
					Gfx.rgba(Gfx.WHITE, Math.min(sweep, 1F - sweep) * 2F * 0.6F * alpha));
		}
	}

	/** Moon on the left, hopping bed on the right, rising Zzz above the bed. */
	private static void icons(GuiGraphicsExtractor g, float t, float alpha, float cx, float cy) {
		// moon: breathes, tilts, and sits in its own bloom
		float moonX = cx - PANEL_W / 2F + 24F;
		float moonY = cy - 2F + Gfx.swing(t, 62F) * 1.8F;
		Gfx.sprite(g, SsTextures.GLOW, moonX, moonY, 62F, 62F, 256, 256,
				1F + 0.10F * Gfx.wave(t, 39F), 0F, Gfx.rgba(Gfx.GOLD, alpha * 0.30F));
		Gfx.sprite(g, SsTextures.MOON, moonX, moonY, 26F, 26F, 128, 128,
				(1F + 0.05F * Gfx.wave(t, 51F)) * Gfx.lerp(Gfx.easeOutBack((t - 6F) / 16F), 0.4F, 1F),
				Gfx.swing(t, 130F) * 0.16F, Gfx.rgba(Gfx.WHITE, alpha));

		// bed: hops with a squash-and-stretch
		float bedX = cx + PANEL_W / 2F - 24F;
		float hop = Math.abs(Gfx.swing(t, 46F));
		float bedY = cy + 3F - hop * 3F;
		float squash = 1F + (0.5F - hop) * 0.10F;
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(bedX, bedY);
		pose.scale(1F / squash, squash);
		Gfx.sprite(g, SsTextures.BED, 0F, 0F, 27F, 27F, 128, 128,
				Gfx.lerp(Gfx.easeOutBack((t - 9F) / 16F), 0.4F, 1F), 0F, Gfx.rgba(Gfx.WHITE, alpha));
		pose.popMatrix();

		// three Zzz drifting up out of the bed forever
		for (int i = 0; i < 3; i++) {
			float p = ((t * 0.9F + i * 21F) % 63F) / 63F;
			float za = (1F - p) * alpha * 0.95F * Gfx.smoothstep(0F, 0.12F, p);
			Gfx.sprite(g, SsTextures.ZZZ, bedX + 6F + Gfx.swing(p * 2F + i, 1.6F) * 3F, bedY - 12F - p * 15F,
					15F, 15F, 128, 128, 0.55F + p * 0.5F, Gfx.swing(p + i, 3F) * 0.25F,
					Gfx.rgba(Gfx.mixRgb(p, Gfx.LAVENDER, Gfx.GOLD), za));
		}
	}

	/** Eight sparkles orbiting the plate, each cycling through the 2x2 sprite sheet. */
	private static void sparkles(GuiGraphicsExtractor g, float t, float alpha, float cx, float cy) {
		for (int i = 0; i < 8; i++) {
			float angle = t * 0.042F + i * (float) (Math.PI * 2.0 / 8.0);
			float radius = PANEL_W / 2F + 5F + 7F * Gfx.wave(t + i * 17F, 55F);
			float sx = cx + (float) Math.cos(angle) * radius;
			float sy = cy + (float) Math.sin(angle) * (PANEL_H / 2F + 13F);

			int frame = (int) ((t / 4F) + i) % 4;
			float u = (frame % 2) * 128F;
			float v = (frame / 2) * 128F;
			float twinkle = 0.35F + 0.65F * Gfx.wave(t + i * 7F, 23F);

			Gfx.spriteRegion(g, SsTextures.SPARKLES, sx, sy, 15F, 15F, u, v, 128, 128, 256, 256,
					0.7F + 0.5F * twinkle, angle * 0.6F, Gfx.rgba(Gfx.WHITE, alpha * twinkle));
		}
	}

	/**
	 * "you can sleep now." - typed out letter by letter, then held on one straight baseline.
	 *
	 * <p>Every glyph is scaled around the same bottom-centre anchor, so the pop-in never makes
	 * the line look crooked; the only motion left afterwards is a shared colour shimmer.</p>
	 */
	private static void message(GuiGraphicsExtractor g, Font font, float t, float alpha, float cx, float cy) {
		String text = SlumberSignal.MESSAGE;
		float reveal = Gfx.clamp01((t - 10F) / 26F);
		int shown = Math.max(0, Math.round(reveal * text.length()));

		if (shown <= 0) {
			return;
		}

		final float scale = 1.0F;
		// the advance widths are summed exactly the way the loop below walks them, so the line
		// is always perfectly centred
		float totalWidth = 0F;

		for (int i = 0; i < text.length(); i++) {
			totalWidth += font.width(String.valueOf(text.charAt(i))) * scale;
		}

		float x = cx - totalWidth / 2F;
		// one shared baseline for the whole line
		float baseline = cy + 6F;

		for (int i = 0; i < shown; i++) {
			String ch = String.valueOf(text.charAt(i));
			float charWidth = font.width(ch) * scale;

			if (ch.equals(" ")) {
				x += charWidth;
				continue;
			}

			float appear = Gfx.clamp01((reveal * text.length()) - i);
			float letterScale = scale * Gfx.lerp(Gfx.easeOutBack(appear), 1.35F, 1F);
			int colour = Gfx.mixRgb(Gfx.wave(t - i * 3F, 70F), Gfx.LAVENDER, Gfx.GOLD);

			Matrix3x2fStack pose = g.pose();
			pose.pushMatrix();
			// anchor = bottom centre of the glyph slot -> the baseline never moves
			pose.translate(x + charWidth / 2F, baseline);
			pose.scale(letterScale, letterScale);
			g.text(font, ch, -font.width(ch) / 2, -font.lineHeight, Gfx.rgba(colour, alpha * appear), true);
			pose.popMatrix();

			x += charWidth;
		}
	}

	/** Thin bar showing how long the toast will stay. */
	private static void countdown(GuiGraphicsExtractor g, float t, float alpha, float cx, float cy) {
		float left = 1F - Gfx.clamp01(t / TOTAL);
		int w = 132;
		int x = Math.round(cx) - w / 2;
		int y = Math.round(cy + PANEL_H / 2F - 8F);

		g.fill(x, y, x + w, y + 1, Gfx.rgba(Gfx.INDIGO, alpha * 0.75F));
		g.fill(x, y, x + Math.round(w * left), y + 1,
				Gfx.rgba(Gfx.mixRgb(Gfx.wave(t, 40F), Gfx.GOLD, Gfx.WHITE), alpha * 0.95F));
	}
}
