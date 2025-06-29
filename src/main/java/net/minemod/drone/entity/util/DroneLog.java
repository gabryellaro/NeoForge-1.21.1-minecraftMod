package net.minemod.drone.entity.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DroneLog {
    private static final Path LOG_DIR = Path.of("logs");
    private static final Path GENERAL_LOG = LOG_DIR.resolve("drone_log.txt");
    private static final Path LANDING_LOG = LOG_DIR.resolve("drone_landing_log.txt");

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logGeneral(String message) {
        logToFile(GENERAL_LOG, message);
    }

    public static void logLanding(String message) {
        logToFile(LANDING_LOG, message);
    }

    private static void logToFile(Path path, String message) {
        try {
            Files.createDirectories(LOG_DIR);
            try (FileWriter writer = new FileWriter(path.toFile(), true)) {
                String timestamp = LocalDateTime.now().format(FORMATTER);
                writer.write("[" + timestamp + "] " + message + System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Erro ao escrever no log (" + path + "): " + e.getMessage());
        }
    }
}
