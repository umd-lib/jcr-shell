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
package org.onehippo.forge.jcrshell.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.output.Output;
import org.onehippo.forge.jcrshell.output.TextOutput;

import static org.onehippo.forge.jcrshell.output.Output.out;

/**
 * Run a query.
 */
public class Query extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.STRING };

    public Query() {
        super("query", new String[] { "select" }, "query <sql|xpath> <statement> [limit <count>]",
                "run a query statement. Language can be xpath or sql.", ARGUMENTS);
    }

    private String dashes(int length) {
        if (length < 20) {
            return "--------------------".substring(0, length);
        } else {
            return "--------------------";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean executeCommand(final String[] args) {
        String language;
        StringBuffer query = new StringBuffer();

        int queryLenght = args.length;
        int max = Integer.MAX_VALUE;

        if ("limit".equals(args[args.length - 2])) {
            queryLenght = args.length - 2;
            try {
                max = Integer.valueOf(args[args.length - 1]);
            } catch (NumberFormatException e) {
                JcrShellPrinter.println("Invalid limit '" + args[args.length - 1] + "'.");
                return false;
            }
        }

        if ("select".equals(args[0])) {
            language = "sql";
            for (int i = 0; i < queryLenght; i++) {
                query.append(args[i]).append(" ");
            }
        } else {
            language = args[1].toLowerCase(Locale.ENGLISH);
            if (!"xpath".equals(language) && !"sql".equals(language)) {
                JcrShellPrinter.println("Unknown query language: " + language);
                return false;
            }
            for (int i = 2; i < queryLenght; i++) {
                query.append(args[i]).append(" ");
            }
        }

        RowIterator iter = null;
        String[] columns = null;
        try {
            QueryResult result = JcrWrapper.query(query.toString(), language);
            if (result != null) {
                columns = result.getColumnNames();
                iter = result.getRows();
            }
        } catch (InvalidQueryException e1) {
            JcrShellPrinter.printWarnln("Invalid query: " + query.toString());
            return false;
        } catch (RepositoryException e) {
            JcrShellPrinter.printErrorln("Error: " + e.getMessage());
            return false;
        }

        if (iter == null || columns == null) {
            JcrShellPrinter.printWarnln("Failed to run query: " + query.toString());
            return false;
        }

        Map<String, Integer> usedColumns = new TreeMap<String, Integer>();
        usedColumns.put("jcr:name", 20);
        List<Map<String, String>> results = new LinkedList<Map<String, String>>();
        int count = 0;
        try {
            while (iter.hasNext() && count < max) {
                Row row = iter.nextRow();
                String path = row.getValue("jcr:path").getString();
                try {
                    Map<String, String> map = new TreeMap<String, String>();
                    map.put("jcr:path", path);
                    Node n = JcrWrapper.getCurrentNode().getSession().getRootNode();
                    if (!"/".equals(path)) {
                        n = n.getNode(path.substring(1));
                    }
                    String fullName = JcrWrapper.fullName(n);
                    map.put("jcr:name", fullName);
                    if (fullName.length() > usedColumns.get("jcr:name")) {
                        usedColumns.put("jcr:name", fullName.length());
                    }
                    for (String name : columns) {
                        if ("jcr:score".equals(name) || "jcr:path".equals(name)) {
                            continue;
                        }
                        Value value = row.getValue(name);
                        if (value != null) {
                            // FIXME: centralize value printing
                            String strVal;
                            if (value.getType() == PropertyType.BINARY) {
                                strVal = "[binary]";
                            } else {
                                strVal = value.getString();
                            }
                            if (usedColumns.containsKey(name)) {
                                Integer length = usedColumns.get(name);
                                if (length < strVal.length()) {
                                    usedColumns.put(name, strVal.length());
                                }
                            } else {
                                if (strVal.length() > 10) {
                                    usedColumns.put(name, strVal.length());
                                } else {
                                    usedColumns.put(name, 10);
                                }
                            }
                            map.put(name, strVal);
                        }
                    }
                    results.add(map);
                } catch (RepositoryException e) {
                    JcrShellPrinter.printErrorln("Error: " + e.getMessage());
                }
                count++;
            }
        } catch (RepositoryException e) {
            JcrShellPrinter.printErrorln("Error: " + e.getMessage());
        }

        Map<String, String> formats = new TreeMap<String, String>();
        for (Map.Entry<String, Integer> entry : usedColumns.entrySet()) {
            formats.put(entry.getKey(), "%-" + (entry.getValue() + 1) + "s");
        }

        TextOutput text = Output.out().a(String.format(formats.get("jcr:name"), "Name"));
        for (String name : usedColumns.keySet()) {
            if ("jcr:score".equals(name) || "jcr:path".equals(name) || "jcr:name".equals(name)) {
                continue;
            }
            text.a(String.format(formats.get(name), name + " "));
        }
        JcrShellPrinter.print(text.a(String.format("%-40s", "Path")));

        text = Output.out().a(String.format(formats.get("jcr:name"), dashes(usedColumns.get("jcr:name"))));
        for (Entry<String, Integer> e : usedColumns.entrySet()) {
            String name = e.getKey();
            if ("jcr:score".equals(name) || "jcr:path".equals(name) || "jcr:name".equals(name)) {
                continue;
            }
            text.a(String.format(formats.get(name), dashes(e.getValue())));
        }
        JcrShellPrinter.print(text.a(String.format("%-40s", "--------------------")));

        for (Map<String, String> row : results) {
            text = Output.out().a(String.format(formats.get("jcr:name"), row.get("jcr:name")));
            for (String name : usedColumns.keySet()) {
                if ("jcr:score".equals(name) || "jcr:path".equals(name) || "jcr:name".equals(name)) {
                    continue;
                }
                String value = row.get(name);
                if (value != null) {
                    text.a(String.format(formats.get(name), value));
                } else {
                    text.a(String.format(formats.get(name), ""));
                }
            }
            JcrShellPrinter.print(text.a(String.format("%-40s", row.get("jcr:path"))));
        }

        text = Output.out().a(String.format(formats.get("jcr:name"), dashes(usedColumns.get("jcr:name"))));
        for (Entry<String, Integer> e : usedColumns.entrySet()) {
            String name = e.getKey();
            if ("jcr:score".equals(name) || "jcr:path".equals(name) || "jcr:name".equals(name)) {
                continue;
            }
            text.a(String.format(formats.get(name), dashes(e.getValue())));
        }
        JcrShellPrinter.print(text.a(String.format("%-40s", "--------------------")));

        JcrShellPrinter.println(String.format("Total: %s", iter.getSize()));
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length >= 3;
    }
}
