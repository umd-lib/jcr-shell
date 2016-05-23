package org.onehippo.forge.jcrshell.output;

public class OkOutput extends Output {

    OkOutput(Output previous) {
        super(previous);
    }

    @Override
    public OkOutput a(String text) {
        return (OkOutput) super.a(text);
    }

    public TextOutput end() {
        return new TextOutput(this);
    }
}
