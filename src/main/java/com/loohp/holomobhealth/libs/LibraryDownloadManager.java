/*
 * This file is part of InteractiveChatDiscordSrvAddon.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.holomobhealth.libs;

import com.loohp.holomobhealth.utils.HTTPRequestUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class LibraryDownloadManager {

    public static final List<String> DEPENDENCIES = Collections.unmodifiableList(Arrays.asList(
        "https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/15.4/nashorn-core-15.4.jar",
        "https://repo1.maven.org/maven2/org/ow2/asm/asm/7.3.1/asm-7.3.1.jar",
        "https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/7.3.1/asm-commons-7.3.1.jar",
        "https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/7.3.1/asm-tree-7.3.1.jar",
        "https://repo1.maven.org/maven2/org/ow2/asm/asm-util/7.3.1/asm-util-7.3.1.jar",
        "https://repo1.maven.org/maven2/org/ow2/asm/asm-analysis/7.3.1/asm-analysis-7.3.1.jar"
    ));

    private File libsFolder;

    public LibraryDownloadManager(File libsFolder) {
        this.libsFolder = libsFolder;
    }

    public synchronized void downloadLibraries(BiConsumer<Boolean, String> progressListener) {
        try {
            libsFolder.mkdirs();
            Set<String> jarNames = new HashSet<>();
            for (String dependency : DEPENDENCIES) {
                String jarName = dependency.substring(dependency.lastIndexOf("/") + 1);
                File jarFile = new File(libsFolder, jarName);
                jarNames.add(jarName);
                jarNames.add(jarName.substring(0, jarName.length() - 4) + "-remapped.jar");
                if (!jarFile.exists()) {
                    if (HTTPRequestUtils.download(jarFile, dependency)) {
                        progressListener.accept(true, jarName);
                    } else {
                        progressListener.accept(false, jarName);
                    }
                }
            }
            for (File jarFile : libsFolder.listFiles()) {
                if (!jarNames.contains(jarFile.getName())) {
                    jarFile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
