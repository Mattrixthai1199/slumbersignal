package com.slumbersignal;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared constants for SlumberSignal.
 *
 * <p>SlumberSignal is a purely client-side mod: it never talks to the server and it never
 * writes a configuration file.</p>
 */
public final class SlumberSignal {
	public static final String MOD_ID = "slumbersignal";
	public static final String MOD_NAME = "SlumberSignal";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	/** The one and only message the mod shows. */
	public static final String MESSAGE = "you can sleep now.";

	private SlumberSignal() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
