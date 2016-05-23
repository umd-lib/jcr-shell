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

import org.apache.commons.io.IOUtils;
import org.onehippo.forge.jcrshell.Command.ArgumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Helper class for commands to map command to it's class and map aliases.
 * This class also handles the reflection stuff for executing commands.
 */
public final class CommandHelper {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(CommandHelper.class);
    
    /**
     * Hide constructor.
     */
    private CommandHelper() {
        super();
    }

    /**
     * The map containing command->class sets.
     */
    private static Map<String, String> commandMap = new TreeMap<String, String>();

    /**
     * The map containing alias->command sets.
     */
    private static Map<String, String> aliasMap = new TreeMap<String, String>();

    private static Map<String, ArgumentType[]> argumentMap = new TreeMap<String, ArgumentType[]>();
    
    /**
     * Check if the alias is a registered alias.
     * @param alias the alias
     * @return true if the alias if found
     */
    public static boolean isAlias(final String alias) {
        return aliasMap.containsKey(alias);
    }

    /**
     * Get the command for the alias.
     * @param alias the alias
     * @return the command or null if the alias is not found
     */
    public static String getCommandForAlias(final String alias) {
        if (isAlias(alias)) {
            return aliasMap.get(alias);
        }
        return null;
    }

    /**
     * Get the aliases for the command.
     * @param command the command
     * @return the array of aliases
     */
    public static String[] getAliasesForCommand(final String command) {
        Set<String> aliases = new HashSet<String>();
        
        for (String alias : aliasMap.keySet()) {
            if (command.equals(getCommandForAlias(alias))) {
                aliases.add(alias);
            }
        }
        return aliases.toArray(new String[aliases.size()]);
    }

    /**
     * Check if the command is a registered command.
     * @param command the command
     * @return true if the command is found
     */
    public static boolean isCommand(final String command) {
        return commandMap.containsKey(command);
    }

    /**
     * Get the class name for the command.
     * @param command the command
     * @return the class name or null if command is not found
     */
    public static String getClassForCommand(final String command) {
        if (isCommand(command)) {
            return commandMap.get(command);
        } else if (isAlias(command)) {
            return commandMap.get(aliasMap.get(command));
        }
        return null;
    }

    /**
     * Register a new command class.
     * @param clazz the class name
     */
    public static void registerCommandClass(final String clazz) {
        Command command = getCommandInstanceForClass(clazz);
        if (command == null) {
            return;
        }
        String cmd = command.getCommand();
        String[] aliases = command.getAliases();
        commandMap.put(cmd, clazz);
        for (String alias : aliases) {
            aliasMap.put(alias, cmd);
        }
        argumentMap.put(cmd, command.getArgumentTypes());
    }

    public static Command getCommandInstance(final String command) {
        return getCommandInstanceForClass(getClassForCommand(command));
    }
    
    public static Command getCommandInstanceForClass(final String clazz) {
        try {
            return (Command) Class.forName(clazz).newInstance();
        } catch (InstantiationException e) {
            log.error("Unable to instantiate class '{}': {}", clazz, e.getMessage());
        } catch (IllegalAccessException e) {
            log.error("No access to class '{}': {}", clazz, e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Class not found '{}': {}", clazz, e.getMessage());
        }
        return null;
    }
    
    /**
     * Get the registered commands as array.
     * @return a string array with the commands
     */
    public static String[] getCommandsAsArray() {
        Set<String> commandSet = commandMap.keySet();
        String[] commands = new String[commandSet.size()];
        commandSet.toArray(commands);
        return commands;
    }

    /**
     * Get the argument types for a command.
     * @return the array of argument types 
     */
    public static ArgumentType[] getArgumentTypes(String command) {
        return argumentMap.get(command);
    }
    
    /**
     * Get the registered aliases as array.
     * @return a string array with the aliases
     */
    public static String[] getAliasesAsArray() {
        Set<String> aliasSet = aliasMap.keySet();
        String[] aliases = new String[aliasSet.size()];
        aliasSet.toArray(aliases);
        return aliases;
    }

    public static void loadCommandsFromResource(String resourceName) {
        InputStream is = CommandHelper.class.getResourceAsStream(resourceName);
        if (is != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                while (br.ready()) {
                    try {
                        String clazz = br.readLine().trim();
                        if (clazz != null && clazz.length() != 0) {
                            CommandHelper.registerCommandClass(clazz);
                        }
                    } catch (IOException e) {
                        log.error("Error while reading line from '{}': {}", resourceName, e.getMessage());
                        log.debug("Stack:", e);
                    }
                }
            } catch (IOException e) {
                log.error("Error while reading commands from '{}': {}", resourceName, e.getMessage());
                log.debug("Stack:", e);
            } finally {
                IOUtils.closeQuietly(br);
                IOUtils.closeQuietly(is);
            }
        } else {
            log.info("Commands file not found on classpath: '{}'", resourceName);
        }
    }
}
