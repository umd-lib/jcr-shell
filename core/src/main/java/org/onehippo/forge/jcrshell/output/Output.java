package org.onehippo.forge.jcrshell.output;

public abstract class Output {

    public static TextOutput out() {
        return new TextOutput();
    }

    private Output next;
    private Output previous;
    private StringBuilder sb = new StringBuilder();

    Output(Output previous) {
        if (previous != null) {
            this.previous = previous;
            previous.next = this;
        }
    }

    public boolean hasNext() {
        return next != null;
    }

    public Output next() {
        return next;
    }

    public Output head() {
        if (previous != null) {
            return previous.head();
        } else {
            return this;
        }
    }

    public Output a(String text) {
        sb.append(text);
        return this;
    }

    public String getText() {
        return sb.toString();
    }
}
