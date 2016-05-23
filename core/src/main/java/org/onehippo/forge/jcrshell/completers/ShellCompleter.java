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
package org.onehippo.forge.jcrshell.completers;

import jline.console.completer.*;
import jline.console.completer.ArgumentCompleter.ArgumentList;
import org.onehippo.forge.jcrshell.Command.ArgumentType;
import org.onehippo.forge.jcrshell.CommandHelper;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Completer for the shell.
 */
public class ShellCompleter implements Completer {

    private ArgumentCompleter.ArgumentDelimiter delim;
    private ArgumentCompleter commandComp;
    private Map<String, ArgumentCompleter> paramCompletors;

    private boolean initialized = false;

    public ShellCompleter() {
    }

    private void init() {
        if (initialized) {
            return;
        } else {
            initialized = true;
        }

        final Completer cmdOrAliasCompleter = new AggregateCompleter(new Completer[] {
                new StringsCompleter(CommandHelper.getCommandsAsArray()),
                new StringsCompleter(CommandHelper.getAliasesAsArray()), new StringsCompleter("") });

        commandComp = new ArgumentCompleter(new Completer[] { cmdOrAliasCompleter });
        paramCompletors = new TreeMap<String, ArgumentCompleter>();

        delim = new ArgumentCompleter.WhitespaceArgumentDelimiter();

        for (String command : CommandHelper.getCommandsAsArray()) {
            ArgumentType[] types = CommandHelper.getArgumentTypes(command);
            Completer[] completors;
            if (types != null) {
                completors = new Completer[types.length + 1];
                completors[0] = cmdOrAliasCompleter;
                int i = 1;
                for (ArgumentType type : types) {
                    Completer completor = null;
                    for (ArgumentType.Flags flag : ArgumentType.Flags.values()) {
                        if (type.getFlags().contains(flag)) {
                            switch (flag) {
                            case NODE:
                                completor = new NodeNameCompleter(type);
                                break;
                            case PROPERTY:
                                completor = new AggregateCompleter(new Completer[] { new PropertyNameCompleter(type),
                                        new NodeNameCompleter() });
                                break;
                            case PRIMITIVE:
                                completor = new StringsCompleter(new String[] { "String", "Boolean", "Long", "Date",
                                        "Binary", "Reference", "Name", "Path" });
                                break;
                            case VERSION:
                                completor = new VersionCompleter(type);
                                break;
                            case COMMAND:
                                completor = new StringsCompleter(CommandHelper.getCommandsAsArray());
                                break;
                            case PRIMARY_TYPE:
                            case MIXIN:
                            case TYPE:
                                completor = new NodeTypeCompleter(type);
                                break;
                            default:
                                Class<? extends Completer> clazz = CompleterFactory.getCompleter(flag);
                                if (clazz != null) {
                                    try {
                                       completor = clazz.newInstance();
                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (completor != null) {
                                break;
                            }
                        }
                    }
                    if (completor == null) {
                        String[] options = type.getOptions();
                        if (options.length == 0) {
                            completor = new NullCompleter();
                        } else {
                            completor = new StringsCompleter(options);
                        }
                    }
                    completors[i++] = completor;
                }
            } else {
                completors = new Completer[1];
                completors[0] = cmdOrAliasCompleter;
            }
            paramCompletors.put(command, new ArgumentCompleter(completors));
        }
    }

    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        init();

        ArgumentList list = delim.delimit(buffer, cursor);
        int argIndex = list.getCursorArgumentIndex();

        if (argIndex < 0) {
            return -1;
        }

        if (argIndex == 0) {
            if ("".equals(buffer)) {
                for (String command : CommandHelper.getCommandsAsArray()) {
                    candidates.add(command);
                }
                return 0;
            }
            return commandComp.complete(buffer, cursor, candidates);
        }

        String[] arguments = list.getArguments();
        if (arguments.length == 0) {
            return -1;
        }

        String command = arguments[0];
        if (!CommandHelper.isCommand(command)) {
            if (!CommandHelper.isAlias(command)) {
                return -1;
            } else {
                command = CommandHelper.getCommandForAlias(command);
            }
        }

        ArgumentCompleter paramCompletor = paramCompletors.get(command);
        if (paramCompletor != null) {
            return paramCompletor.complete(buffer, cursor, candidates);
        }
        return -1;
    }

}
