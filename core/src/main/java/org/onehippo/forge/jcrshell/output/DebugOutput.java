package org.onehippo.forge.jcrshell.output;

public class DebugOutput extends Output {

    DebugOutput(Output previous) {
        super(previous);
    }

    @Override
    public DebugOutput a(String text) {
        return (DebugOutput) super.a(text);
    }

    public TextOutput end() {
        return new TextOutput(this);
    }

}
