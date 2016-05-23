package org.onehippo.forge.jcrshell.output;

public class TextOutput extends Output {

    TextOutput() {
        super(null);
    }

    TextOutput(Output previous) {
        super(previous);
    }

    public TextOutput a(String text) {
        return (TextOutput) super.a(text);
    }

    public OkOutput ok() {
        return new OkOutput(this);
    }

    public TextOutput ok(String text) {
        OkOutput ok = new OkOutput(this);
        ok.a(text);
        return ok.end();
    }

    public WarnOutput warn() {
        return new WarnOutput(this);
    }

    public TextOutput warn(String text) {
        WarnOutput warn = new WarnOutput(this);
        warn.a(text);
        return warn.end();
    }

    public ErrorOutput error() {
        return new ErrorOutput(this);
    }

    public TextOutput error(String text) {
        ErrorOutput error = new ErrorOutput(this);
        error.a(text);
        return error.end();
    }

    public DebugOutput debug() {
        return new DebugOutput(this);
    }

    public TextOutput debug(String text) {
        return new DebugOutput(this).a(text).end();
    }

}
