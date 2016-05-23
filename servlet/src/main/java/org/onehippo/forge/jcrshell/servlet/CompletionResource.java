package org.onehippo.forge.jcrshell.servlet;

import jline.console.completer.Completer;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.completers.ShellCompleter;

import javax.jcr.RepositoryException;
import javax.ws.rs.*;
import java.util.LinkedList;
import java.util.List;

@Path("/{path:.*}")
@Produces("application/json")
public class CompletionResource {

    private static final Completer completer = new ShellCompleter();

    @GET
    public Completion complete(@PathParam("path") String path,
                               @QueryParam("current") String current,
                               @QueryParam("cursor") @DefaultValue("-1") int cursor) {
        try {
            JcrWrapper.cd("/" + path);
        } catch (RepositoryException e) {
            throw new WebApplicationException(e);
        }
        List<CharSequence> candidates = new LinkedList<CharSequence>();
        int start = completer.complete(current, cursor < 0 ? current.length() : cursor, candidates);

        Completion completion = new Completion();
        completion.setStart(start);
        List<String> asStrings = completion.getCandidates();
        for (CharSequence seq : candidates) {
            asStrings.add(seq.toString());
        }
        return completion;
    }

}
