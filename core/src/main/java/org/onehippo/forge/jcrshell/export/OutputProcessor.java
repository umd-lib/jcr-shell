/*
 *  Copyright 2009 Hippo.
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
package org.onehippo.forge.jcrshell.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class OutputProcessor extends XmlProcessor {
    private static final String SV_VALUE = "sv:value";
    private int indent = 0;
    private StringBuffer textBuffer = null;
    private PrintWriter out;
    private boolean inValue = false;

    OutputProcessor(SAXParserFactory factory, XmlProcessor recordProcessor, InputStream istream, OutputStream ostream)
            throws IOException, ParserConfigurationException, SAXException {
        super(factory, recordProcessor, istream);
        out = new PrintWriter(new OutputStreamWriter(ostream, "UTF8"));
    }

    OutputProcessor(SAXParserFactory factory, InputStream istream, OutputStream ostream) throws IOException,
            ParserConfigurationException, SAXException {
        super(factory, null, istream);
        out = new PrintWriter(new OutputStreamWriter(ostream, "UTF8"));
    }

    @Override
    void process() throws IOException, SAXException {
        super.process();
        out.flush();
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            out.flush();
        } finally {
            super.endDocument();
        }
    }

    private void flush() throws SAXException {
        if (textBuffer == null) {
            return;
        }
        String s = "" + textBuffer;
        s = s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        out.print(s);
        textBuffer = null;
    }

    private void clear() {
        textBuffer = null;
    }

    @Override
    protected void insert(String literal) throws SAXException {
        String whitespace = getLiterals().get(new XMLLocation(XMLItemType.WHITESPACE, getPath(), false));
        if (whitespace != null) {
            out.print(whitespace);
        }
        if (literal.trim().contains("\n")) {
            out.print("\n<!--");
            out.print(literal);
            out.print("-->\n\n");
        } else {
            for (int i = 0; i < indent; i++) {
                out.print("  ");
            }
            out.print("<!--");
            out.print(literal);
            out.print("-->\n");
        }
        whitespace = getLiterals().get(new XMLLocation(XMLItemType.WHITESPACE, getPath(), true));
        if (whitespace != null) {
            out.print(whitespace);
        }
    }

    @Override
    public void characters(char buf[], int offset, int len) throws SAXException {
        if (inValue) {
            String s = new String(buf, offset, len);
            if (textBuffer == null) {
                textBuffer = new StringBuffer(s);
            } else {
                textBuffer.append(s);
            }
        }
    }

    @Override
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
        super.startElement(namespaceURI, sName, qName, attrs);
        flush();
        String eName = sName;
        if ("".equals(eName)) {
            eName = qName;
        }
        if (eName.equals(SV_VALUE)) {
            if (getPath().endsWith("/hippo:paths")) {
                return;
            }
            inValue = true;
        }
        for (int i = 0; i < indent; i++) {
            out.print("  ");
        }
        out.print("<" + eName);
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i);
                if ("".equals(aName)) {
                    aName = attrs.getQName(i);
                }
                if (aName.startsWith("xmlns:") && !"xmlns:sv".equals(aName)) {
                    continue;
                }
                out.print(" ");
                out.print(aName + "=\"" + attrs.getValue(i) + "\"");
            }
        }
        out.print(">");
        if (!eName.equals(SV_VALUE)) {
            out.println();
        }
        ++indent;
    }

    @Override
    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        super.endElement(namespaceURI, sName, qName);
        String eName = sName;
        if ("".equals(eName)) {
            eName = qName;
        }
        if (eName.equals(SV_VALUE)) {
            if (getPath().endsWith("/hippo:paths")) {
                clear();
                return;
            }
            inValue = false;
        }
        flush();
        --indent;
        if (!eName.equals(SV_VALUE)) {
            for (int i = 0; i < indent; i++) {
                out.print("  ");
            }
        }
        out.println("</" + eName + ">");
    }
}
