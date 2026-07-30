package com.slumbersignal.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

/** Small maths + blit helpers used by the animated toast. */
public final class Gfx {
	public static final int LAVENDER = 0xB8C6FF;
	public static final int GOLD = 0xFFD98A;
	public static final int WHITE = 0xFFFFFF;
	public static final int INDIGO = 0x12163A;

	private Gfx() {
	}

	// ---------------------------------------------------------------- maths

	public static float clamp01(float v) {
		return v < 0F ? 0F : (v > 1F ? 1F : v);
	}

	public static float lerp(float t, float a, float b) {
		return a + (b - a) * t;
	}

	public static float smoothstep(float edge0, float edge1, float x) {
		float t = clamp01((x - edge0) / (edge1 - edge0));
		return t * t * (3F - 2F * t);
	}

	public static float easeOutCubic(float t) {
		float u = 1F - clamp01(t);
		return 1F - u * u * u;
	}

	public static float easeInCubic(float t) {
		float u = clamp01(t);
		return u * u * u;
	}

	/** Overshoot easing - the toast snaps past its target and settles back. */
	public static float easeOutBack(float t) {
		float u = clamp01(t) - 1F;
		float c = 1.70158F;
		return 1F + (c + 1F) * u * u * u + c * u * u;
	}

	public static float easeOutElastic(float t) {
		float u = clamp01(t);

		if (u == 0F || u == 1F) {
			return u;
		}

		return (float) (Math.pow(2, -10 * u) * Math.sin((u * 10 - 0.75) * (2 * Math.PI / 3)) + 1);
	}

	/** 0..1 sine wave with the given period in ticks. */
	public static float wave(float time, float periodTicks) {
		return (float) (0.5 + 0.5 * Math.sin(time / periodTicks * Math.PI * 2.0));
	}

	/** -1..1 sine wave. */
	public static float swing(float time, float periodTicks) {
		return (float) Math.sin(time / periodTicks * Math.PI * 2.0);
	}

	// ---------------------------------------------------------------- colour

	public static int rgba(int rgb, float alpha) {
		int a = Math.round(clamp01(alpha) * 255F);
		return ARGB.color(a, (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
	}

	public static int mixRgb(float t, int a, int b) {
		int r = Math.round(lerp(t, (a >> 16) & 0xFF, (b >> 16) & 0xFF));
		int g = Math.round(lerp(t, (a >> 8) & 0xFF, (b >> 8) & 0xFF));
		int bl = Math.round(lerp(t, a & 0xFF, b & 0xFF));
		return (r << 16) | (g << 8) | bl;
	}

	// ---------------------------------------------------------------- blits

	/**
	 * Draws a whole texture centred on (cx, cy), scaled and rotated. Position is applied through the
	 * matrix stack so movement stays sub-pixel smooth.
	 */
	public static void sprite(GuiGraphicsExtractor g, Identifier tex, float cx, float cy,
			float width, float height, int texW, int texH, float scale, float rotation, int argb) {
		spriteRegion(g, tex, cx, cy, width, height, 0F, 0F, texW, texH, texW, texH, scale, rotation, argb);
	}

	/** Draws one region of a texture (used for the 2x2 sparkle sheet). */
	public static void spriteRegion(GuiGraphicsExtractor g, Identifier tex, float cx, float cy,
			float width, float height, float u, float v, int regionW, int regionH,
			int texW, int texH, float scale, float rotation, int argb) {
		int w = Math.max(1, Math.round(width));
		int h = Math.max(1, Math.round(height));

		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);

		if (rotation != 0F) {
			pose.rotate(rotation);
		}

		if (scale != 1F) {
			pose.scale(scale, scale);
		}

		g.blit(RenderPipelines.GUI_TEXTURED, tex, -w / 2, -h / 2, u, v, w, h, regionW, regionH, texW, texH, argb);
		pose.popMatrix();
	}

	/** Draws a texture with its top-left corner at (x, y), no rotation. */
	public static void spriteAt(GuiGraphicsExtractor g, Identifier tex, float x, float y,
			float width, float height, int texW, int texH, int argb) {
		int w = Math.max(1, Math.round(width));
		int h = Math.max(1, Math.round(height));

		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		g.blit(RenderPipelines.GUI_TEXTURED, tex, 0, 0, 0F, 0F, w, h, texW, texH, texW, texH, argb);
		pose.popMatrix();
	}
}
