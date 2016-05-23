package org.onehippo.forge.jcrshell.output;

public class ErrorOutput extends Output {

    ErrorOutput(Output previous) {
        super(previous);
    }

    @Override
    public ErrorOutput a(String text) {
        return (ErrorOutput) super.a(text);
    }

    public TextOutput end() {
        return new TextOutput(this);
    }
}
