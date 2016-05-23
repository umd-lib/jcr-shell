package org.onehippo.forge.jcrshell.completers;

import jline.console.completer.Completer;
import org.onehippo.forge.jcrshell.Command;

import java.util.HashMap;

public final class CompleterFactory {

    static final HashMap<Command.ArgumentType.Flags, Class<? extends Completer>> completers = new HashMap();

    public static void registerCompleter(Command.ArgumentType.Flags type, Class<? extends Completer> clazz) {
        completers.put(type, clazz);
    }

    public static Class<? extends Completer> getCompleter(Command.ArgumentType.Flags type) {
        return completers.get(type);
    }
}
