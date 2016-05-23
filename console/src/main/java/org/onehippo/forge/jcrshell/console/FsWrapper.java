/*
 *  Copyright 2010 Hippo.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.jcrshell.console;

import java.io.File;

public final class FsWrapper {
    
    private static File cwd = new File(new File("").getAbsolutePath());

    /**
     * Hide constructor
     */
    private FsWrapper() {
    }

    public static File getUserHome() {
        return new File(System.getProperty("user.home"));
    }

    public static String getFullFileName(String path) {
        String translated = path;
        File homeDir = getUserHome();

        // Special character: ~ maps to the user's home directory
        if (translated.startsWith("~" + File.separator)) {
            translated = homeDir.getPath() + translated.substring(1);
        } else if (translated.startsWith("~")) {
            translated = homeDir.getParentFile().getAbsolutePath();
        } else if (!(translated.startsWith(File.separator))) {
            translated = getCwd().getPath() + File.separator + translated;
        }

        return translated;
    }
    
    public static File getCwd() {
        return cwd;
    }
    
    public static File[] list() {
        File[] list = cwd.listFiles();
        if (list == null) {
            return new File[]{};
        }
        return list;
    }

    public static boolean isRoot() {
        return !new File(cwd, "..").exists();
    }
    
    public static boolean chdir(String path) {
        File newWd = new File(getFullFileName(path));
        if (newWd.exists() && newWd.isDirectory()) {
            cwd = newWd;
            return true;
        } else {
            return false;
        }
    }

}
