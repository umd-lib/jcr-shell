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
package org.onehippo.forge.jcrshell;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Command interface. When invoked, execute is called.
 * The help a will be shown next to the command when
 * the 'help' command is issued.
 */
public interface Command {

    final class ArgumentType {
        private final EnumSet<Flags> flags;
        private String[] options = new String[0];

        public static final ArgumentType COMMAND = new ArgumentType(EnumSet.of(Flags.COMMAND));
        public static final ArgumentType PROPERTY = new ArgumentType(EnumSet.of(Flags.PROPERTY));
        public static final ArgumentType NODE = new ArgumentType(EnumSet.of(Flags.NODE));
        public static final ArgumentType FILE = new ArgumentType(EnumSet.of(Flags.FILE));
        public static final ArgumentType DIRECTORY = new ArgumentType(EnumSet.of(Flags.DIRECTORY));
        public static final ArgumentType UUID = new ArgumentType(EnumSet.of(Flags.UUID));
        public static final ArgumentType TYPE = new ArgumentType(EnumSet.of(Flags.TYPE));
        public static final ArgumentType MIXIN = new ArgumentType(EnumSet.of(Flags.MIXIN));
        public static final ArgumentType USER = new ArgumentType(EnumSet.of(Flags.USER));
        public static final ArgumentType PRIMITIVE = new ArgumentType(EnumSet.of(Flags.PRIMITIVE));
        public static final ArgumentType PRIMARY_TYPE = new ArgumentType(EnumSet.of(Flags.PRIMARY_TYPE));
        public static final ArgumentType STRING = new ArgumentType(EnumSet.of(Flags.STRING));
        public static final ArgumentType INTEGER = new ArgumentType(EnumSet.of(Flags.INTEGER));
        public static final ArgumentType VERSION = new ArgumentType(EnumSet.of(Flags.VERSION));
        public static final ArgumentType PREFIX = new ArgumentType(EnumSet.of(Flags.PREFIX));

        public enum Flags {
            ADD, READ, WRITE, REMOVE,
            MULTI, 
            NO_LABELS, ONLY_LABELS,
            FILE, DIRECTORY,
            COMMAND, PROPERTY, NODE, UUID, TYPE, MIXIN, PRIMARY_TYPE, USER, STRING, INTEGER, PRIMITIVE, VERSION, PREFIX
        }

        public ArgumentType(EnumSet<Flags> flags) {
            this.flags = flags;
        }

        public ArgumentType(String[] options) {
            this.flags = EnumSet.noneOf(Flags.class);
            this.options = options.clone();
        }
        
        public EnumSet<Flags> getFlags() {
            return flags;
        }

        /**
         * A static list of options, or null when the argument type is not static.
         */
        public String[] getOptions() {
            return options.clone();
        }
    }

    /**
     * Get the command string.
     * @return the command
     */
    String getCommand();

    /**
     * Get the command aliases.
     * @return a string array with the aliases
     */
    String[] getAliases();

    /**
     * Get the help message.
     * @return the help about the command
     */
    String help();

    /**
     * Get usage info.
     * @return the usage of the command
     */
    String usage();

    /**
     * Get type of arguments
     * @return list of types of argument
     */
    ArgumentType[] getArgumentTypes();

    /**
     * Execute the command with the given command.
     * @param args the arguments for the command
     * @return true on successful execution
     * @throws RepositoryException when some interaction with the repository fails.
     * @throws IOException when some interaction with the file system fails.
     */
    
    boolean execute(String[] args) throws RepositoryException, IOException;
}
