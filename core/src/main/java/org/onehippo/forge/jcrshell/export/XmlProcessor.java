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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

class XmlProcessor extends DefaultHandler2 {
    private StringBuffer commentBuffer = null;
    private StringBuffer whitespaceBuffer = null;
    private String path = null;
    final private Map<XMLLocation, String> literals;
    private boolean playback;
    private SAXParser saxParser;
    final private InputStream istream;

    XmlProcessor(SAXParserFactory factory, File file) throws IOException, ParserConfigurationException, SAXException {
        this(factory, new FileInputStream(file));
    }

    XmlProcessor(SAXParserFactory factory, InputStream istream) throws IOException, ParserConfigurationException,
            SAXException {
        this.istream = istream;
        saxParser = factory.newSAXParser();
        saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", this);
        literals = new TreeMap<XMLLocation, String>();
        playback = false;
    }
    
    protected XmlProcessor(SAXParserFactory factory, XmlProcessor recordProcessor, InputStream istream)
            throws IOException, ParserConfigurationException, SAXException {
        this.istream = istream;
        saxParser = factory.newSAXParser();
        if (recordProcessor != null) {
            literals = new TreeMap<XMLLocation, String>(recordProcessor.literals);
        } else {
            literals = new TreeMap<XMLLocation, String>();
        }
        playback = true;
    }
    
    void process() throws IOException, SAXException {
        try {
            saxParser.parse(istream, this);
        } catch (SAXException ex) {
            Throwable e = ex.getCause();
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw ex;
            }
        }
        istream.close();
    }

    protected void insert(String literal) throws SAXException {
    }

    private void flush(boolean afterElement) throws SAXException {
        if (playback) {
            XMLLocation location = new XMLLocation(XMLItemType.COMMENT, path, afterElement);
            if (literals.containsKey(location)) {
                insert(literals.get(location));
            }
        } else {
            if (whitespaceBuffer != null) {
                literals.put(new XMLLocation(XMLItemType.WHITESPACE, path, (commentBuffer == null)), new String(
                        whitespaceBuffer));
            }
            whitespaceBuffer = null;
            if (commentBuffer == null) {
                return;
            }
            String s = new String(commentBuffer);
            if (!s.trim().equals("")) {
                literals.put(new XMLLocation(XMLItemType.COMMENT, path, afterElement), s);
            }
            commentBuffer = null;
        }
    }

    @Override
    public void comment(char buf[], int offset, int len) throws SAXException {
        if (!playback) {
            String s = new String(buf, offset, len);
            if (commentBuffer == null) {
                commentBuffer = new StringBuffer(s);
            } else {
                commentBuffer.append(s);
            }
        }
    }

    @Override
    public void ignorableWhitespace(char[] buf, int offset, int len) {
        if (!playback) {
            String s = new String(buf, offset, len);
            if (whitespaceBuffer == null) {
                whitespaceBuffer = new StringBuffer(s);
            } else {
                whitespaceBuffer.append(s);
            }
        }
    }

    @Override
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
        String eName = sName;
        if ("".equals(eName)) {
            eName = qName;
        }
        if ("sv:property".equals(eName) || "property".equals(eName) || "sv:node".equals(eName) || "node".equals(eName)) {
            if (attrs != null) {
                String name = "";
                for (int i = 0; i < attrs.getLength(); i++) {
                    String aName = attrs.getLocalName(i);
                    if ("".equals(aName)) {
                        aName = attrs.getQName(i);
                    }
                    if ("sv:name".equals(aName) || "name".equals(aName)) {
                        name = attrs.getValue(i);
                    }
                }
                if (path != null) {
                    path += "/" + name;
                } else {
                    path = name;
                }
            }
            flush(false);
        }
    }

    @Override
    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        String eName = sName;
        if ("".equals(eName)) {
            eName = qName;
        }
        if ("sv:property".equals(eName) || "property".equals(eName) || "sv:node".equals(eName) || "node".equals(eName)) {
            flush(true);
            int idx = path.lastIndexOf('/');
            if (idx < 0) {
                path = null;
            } else {
                path = path.substring(0, idx);
            }
        }
    }

    @Override
    public void startDocument() throws SAXException {
        flush(false);
    }

    @Override
    public void endDocument() throws SAXException {
        flush(true);
    }

    protected String getPath() {
        return path;
    }

    protected Map<XMLLocation, String> getLiterals() {
        return literals;
    }
}