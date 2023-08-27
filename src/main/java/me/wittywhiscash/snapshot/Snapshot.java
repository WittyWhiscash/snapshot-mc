package me.wittywhiscash.snapshot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Snapshot implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Snapshot");
	public static final me.wittywhiscash.snapshot.SnapshotConfig CONFIG = me.wittywhiscash.snapshot.SnapshotConfig.createAndLoad();
	public static final String TEMPLATE_DIRECTORY = "templates";

	@Override
	public void onInitializeClient() {
		try {
			Path templatePath = Path.of(MinecraftClient.getInstance().runDirectory.getAbsoluteFile().getPath(), TEMPLATE_DIRECTORY);
			if (Files.notExists(templatePath))
				Files.createDirectory(templatePath);
		} catch (IOException e) {
			LOGGER.error("Failed to create template path", e);
		}

	}
}