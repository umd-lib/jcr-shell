package org.onehippo.forge.jcrshell.output;

public class WarnOutput extends Output {

    WarnOutput(Output previous) {
        super(previous);
    }

    @Override
    public WarnOutput a(String text) {
        return (WarnOutput) super.a(text);
    }

    public TextOutput end() {
        return new TextOutput(this);
    }
}
