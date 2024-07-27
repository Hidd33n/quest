package com.narohid.quest.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MissionLoader {

    public static List<FileConfiguration> loadMissions() {
        List<FileConfiguration> missionConfigs = new ArrayList<>();
        File missionsFolder = new File("plugins/Quest/missions");

        if (!missionsFolder.exists() || !missionsFolder.isDirectory()) {
            missionsFolder.mkdirs();
        }

        File[] files = missionsFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        // Crear misiones predeterminadas si no existen misiones
        if (files == null || files.length == 0) {
            createDefaultMissions(missionsFolder);
            files = missionsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        }

        if (files != null) {
            for (File file : files) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.set("fileName", file.getName()); // Almacenar el nombre del archivo en la configuración
                missionConfigs.add(config);
            }
        }

        return missionConfigs;
    }

    private static void createDefaultMissions(File missionsFolder) {
        createMissionFromResource(missionsFolder, "default.yml");
        createMissionFromResource(missionsFolder, "gather.yml");
    }

    private static void createMissionFromResource(File missionsFolder, String resourceName) {
        File missionFile = new File(missionsFolder, resourceName);
        if (!missionFile.exists()) {
            try (InputStream in = MissionLoader.class.getResourceAsStream("/" + resourceName)) {
                if (in != null) {
                    Files.copy(in, missionFile.toPath());
                    System.out.println("Mission " + resourceName + " created successfully.");
                } else {
                    System.out.println("Mission " + resourceName + " not found in resources.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
