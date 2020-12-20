package de.raidcraft.template.util;

import java.io.File;
import java.nio.file.Path;

public class ConfigUtil {

    /**
     * Extracts a file identifier from the two gives paths based on the relative path.
     * The identifier will be lowercased and every subdirectly is split by a dot.
     *
     * @param base the base path
     * @param file the file
     * @return the file identifier
     */
    public static String getFileIdentifier(Path base, File file) {

        Path relativePath = base.relativize(file.toPath());
        return relativePath.toString()
                .replace("/", ".")
                .replace("\\", ".")
                .toLowerCase()
                .replace(".yml", "")
                .replace(".yaml", "");
    }
}
