package com.gtohjs.util;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;

public final class ModLog {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path FILE = Path.of("gtohjs.log");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private ModLog() {
    }

    public static void info(String message, Object... arguments) {
        LOGGER.info(message, arguments);
        write("INFO", format(message, arguments), null);
    }

    public static void error(String message, Object... arguments) {
        LOGGER.error(message, arguments);
        write("ERROR", format(message, arguments), null);
    }

    public static void error(String message, Throwable error) {
        LOGGER.error(message, error);
        write("ERROR", message, error);
    }

    private static synchronized void write(String level, String message, Throwable error) {
        StringBuilder line = new StringBuilder()
                .append('[').append(LocalDateTime.now().format(TIME)).append("] ")
                .append('[').append(level).append("] ")
                .append(message).append(System.lineSeparator());
        if (error != null) {
            line.append(error).append(System.lineSeparator());
            for (StackTraceElement element : error.getStackTrace()) {
                line.append("  at ").append(element).append(System.lineSeparator());
            }
        }
        try {
            Files.writeString(FILE, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    private static String format(String template, Object... arguments) {
        String result = template;
        for (Object argument : arguments) {
            result = result.replaceFirst("\\{}", java.util.regex.Matcher.quoteReplacement(String.valueOf(argument)));
        }
        return result;
    }
}

