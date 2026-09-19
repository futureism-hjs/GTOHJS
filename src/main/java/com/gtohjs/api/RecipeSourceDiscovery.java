package com.gtohjs.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;

/** Deterministically discovers public no-argument recipe sources in {@code com.gtohjs.gtrecipe}. */
final class RecipeSourceDiscovery {
    static final String PACKAGE_NAME = "com.gtohjs.gtrecipe";
    private static final String PACKAGE_PATH = PACKAGE_NAME.replace('.', '/');

    private RecipeSourceDiscovery() {
    }

    static <T> List<T> discover(Class<T> sourceType) {
        List<String> classNames = classNames();
        List<T> sources = new ArrayList<>();
        ClassLoader loader = RecipeSourceDiscovery.class.getClassLoader();
        for (String className : classNames) {
            try {
                Class<?> candidate = Class.forName(className, true, loader);
                if (!sourceType.isAssignableFrom(candidate) || candidate.isInterface()) {
                    continue;
                }
                Object value = candidate.getDeclaredConstructor().newInstance();
                sources.add(sourceType.cast(value));
            } catch (ReflectiveOperationException error) {
                throw new IllegalStateException("Invalid " + sourceType.getSimpleName() +
                        " class " + className, error);
            }
        }
        sources.sort(Comparator.comparing(source -> source.getClass().getName()));
        return List.copyOf(sources);
    }

    private static List<String> classNames() {
        try {
            URL location = RecipeSourceDiscovery.class.getProtectionDomain()
                    .getCodeSource().getLocation();
            URI uri = location.toURI();
            Path root = Paths.get(uri);
            List<String> names = new ArrayList<>();
            if (Files.isDirectory(root)) {
                Path packageDirectory = root.resolve(PACKAGE_PATH);
                if (Files.isDirectory(packageDirectory)) {
                    try (var files = Files.list(packageDirectory)) {
                        files.map(path -> path.getFileName().toString())
                                .filter(name -> name.endsWith(".class") && !name.contains("$"))
                                .map(name -> PACKAGE_NAME + "." +
                                        name.substring(0, name.length() - ".class".length()))
                                .forEach(names::add);
                    }
                }
            } else {
                try (JarFile jar = new JarFile(root.toFile())) {
                    jar.stream().map(entry -> entry.getName())
                            .filter(name -> name.startsWith(PACKAGE_PATH + "/") &&
                                    !name.substring(PACKAGE_PATH.length() + 1).contains("/") &&
                                    name.endsWith(".class") && !name.contains("$"))
                            .map(name -> name.substring(0, name.length() - ".class".length())
                                    .replace('/', '.'))
                            .forEach(names::add);
                }
            }
            names.sort(String::compareTo);
            return List.copyOf(names);
        } catch (IOException | URISyntaxException error) {
            throw new IllegalStateException("Unable to discover recipe source classes", error);
        }
    }
}
