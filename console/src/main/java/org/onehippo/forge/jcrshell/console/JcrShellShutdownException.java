/*
 *  Copyright 2008 Hippo.
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

/**
 * Simple runtime exception for indicating shutdown of the shell.
 */
public class JcrShellShutdownException extends RuntimeException {

    /**
     * Serial.
     */
    private static final long serialVersionUID = -8643561635607100664L;

    /**
     * Just an empty constructor.
     */
    public JcrShellShutdownException() {
        super();
    }

}
