package com.urntt.nojumpdelay.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.urntt.nojumpdelay.NoJumpDelayClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mod's settings, persisted as JSON. Every change is written to disk immediately.
 */
public final class NoJumpDelayConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(NoJumpDelayClient.MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path path;
	private final Settings settings;

	private NoJumpDelayConfig(final Path path, final Settings settings) {
		this.path = path;
		this.settings = settings;
	}

	/**
	 * Returns the location of the config file in the Fabric config directory.
	 */
	public static Path defaultPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(NoJumpDelayClient.MOD_ID + ".json");
	}

	/**
	 * Loads the configuration from {@code path}. A missing file is created with the defaults. An unreadable
	 * file falls back to the defaults and is left untouched until the next change is saved.
	 */
	public static NoJumpDelayConfig load(final Path path) {
		if (Files.notExists(path)) {
			NoJumpDelayConfig config = new NoJumpDelayConfig(path, new Settings());
			config.save();
			return config;
		}

		try {
			Settings settings = GSON.fromJson(Files.readString(path), Settings.class);
			return new NoJumpDelayConfig(path, settings != null ? settings : new Settings());
		} catch (IOException | JsonParseException e) {
			LOGGER.warn("Failed to read config file {}, using defaults", path, e);
			return new NoJumpDelayConfig(path, new Settings());
		}
	}

	public boolean isEnabled() {
		return this.settings.enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.settings.enabled = enabled;
		this.save();
	}

	private void save() {
		try {
			Files.createDirectories(this.path.getParent());
			Files.writeString(this.path, GSON.toJson(this.settings));
		} catch (IOException e) {
			LOGGER.error("Failed to write config file {}", this.path, e);
		}
	}

	/**
	 * The serialized form. Field initializers are the defaults, which also apply to keys missing from the file.
	 */
	private static final class Settings {
		private boolean enabled = true;
	}
}
