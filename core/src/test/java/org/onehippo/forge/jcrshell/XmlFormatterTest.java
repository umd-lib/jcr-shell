/*
 *  Copyright 2010 Hippo.
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
package org.onehippo.forge.jcrshell;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.onehippo.forge.jcrshell.export.XmlFormatter;
import org.xml.sax.SAXException;

public class XmlFormatterTest {
    private String readFileAsString(File file) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    @Test
    public void onlySystemViewNamespaceIsDeclared() throws IOException, ParserConfigurationException, SAXException {
        assertFormat(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" sv:name=\"aap\">\n" +
            "  <sv:property sv:name=\"mies\">\n" +
            "    <sv:value>bla</sv:value>\n"+
            "  </sv:property>\n" +
            "</sv:node>\n",

            "<?xml version=\"1.0\"?>"+
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" xmlns:bla=\"die\" sv:name=\"aap\">"+
            "<sv:property sv:name=\"mies\">"+
            "<sv:value>bla</sv:value>"+
            "</sv:property>"+
            "</sv:node>");
    }

    private void assertFormat(String expected, String input) throws IOException, ParserConfigurationException, SAXException {
        File inputFile = createTempFile("formatter-input", input);
        File output = createTempFile("formatter-out");

        assertFormat(expected, inputFile, output);

        inputFile.delete();
        output.delete();
    }

    private File createTempFile(String name) throws IOException {
        File file = File.createTempFile(name, ".test");
        file.delete();
        return file;
    }

    private void assertFormat(String expected, File inputFile, File output) throws IOException, ParserConfigurationException, SAXException {
        XmlFormatter.format(inputFile, output);

        String content = readFileAsString(output);
        assertEquals(expected, content);
    }

    private File createTempFile(String name, String content) throws IOException {
        File file = File.createTempFile(name, ".test");
        {
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            ps.print(content);
            ps.close();
        }
        return file;
    }

    @Test
    public void testFormatterWithStructure() throws IOException, ParserConfigurationException, SAXException {
        assertFormat(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- header -->\n"+
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" sv:name=\"aap\">\n" +
            "  <!-- hoepla -->\n"+
            "  <sv:property sv:name=\"mies\">\n" +
            "    <sv:value>bla</sv:value>\n"+
            "  </sv:property>\n" +
            "</sv:node>\n", 
            
            "<?xml version=\"1.0\"?>" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" xmlns:bla=\"die\" sv:name=\"aap\">" +
            "<sv:property sv:name=\"mies\">" +
            "<sv:value>bla</sv:value>" +
            "</sv:property>"+
            "</sv:node>",
            
            "<?xml version=\"1.0\"?>\n" +
            "<!-- header -->\n" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" xmlns:bla=\"die\" sv:name=\"aap\">" +
            "  <!-- hoepla -->\n" +
            "  <sv:property sv:name=\"mies\">" +
            "    <sv:value>noot</sv:value>" +
            "  </sv:property>" +
            "</sv:node>");
    }

    private void assertFormat(String expected, String input, String old) throws IOException, ParserConfigurationException, SAXException {
        File inputFile = createTempFile("formatter-input", input);
        File output = createTempFile("formatter-out", old);

        assertFormat(expected, inputFile, output);

        inputFile.delete();
        output.delete();
    }

    @Test
    public void testWhitespaceInHeader() throws IOException, ParserConfigurationException, SAXException {
        assertFormat(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- header \n" + 
            "-->\n"+
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" sv:name=\"aap\">\n" +
            "  <!-- hoepla -->\n"+
            "  <sv:property sv:name=\"mies\">\n" +
            "    <sv:value>bla</sv:value>\n"+
            "  </sv:property>\n" +
            "</sv:node>\n", 
            
            "<?xml version=\"1.0\"?>" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" xmlns:bla=\"die\" sv:name=\"aap\">" +
            "<sv:property sv:name=\"mies\">" +
            "<sv:value>bla</sv:value>" +
            "</sv:property>"+
            "</sv:node>",
            
            "<?xml version=\"1.0\"?>\n" +
            "<!-- header \n" +
            "-->\n" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" xmlns:bla=\"die\" sv:name=\"aap\">" +
            "  <!-- hoepla -->\n" +
            "  <sv:property sv:name=\"mies\">" +
            "    <sv:value>noot</sv:value>" +
            "  </sv:property>" +
            "</sv:node>");
    }

    @Test
    public void testWhitespaceInComment() throws IOException, ParserConfigurationException, SAXException {
        assertFormat(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- header -->\n"+
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" sv:name=\"aap\">\n" +
            "  <!-- hoepla\n" +
            "-->\n"+
            "  <sv:property sv:name=\"mies\">\n" +
            "    <sv:value>bla</sv:value>\n"+
            "  </sv:property>\n" +
            "</sv:node>\n", 
            
            "<?xml version=\"1.0\"?>" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" xmlns:bla=\"die\" sv:name=\"aap\">" +
            "<sv:property sv:name=\"mies\">" +
            "<sv:value>bla</sv:value>" +
            "</sv:property>"+
            "</sv:node>",
            
            "<?xml version=\"1.0\"?>\n" +
            "<!-- header -->\n" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" xmlns:bla=\"die\" sv:name=\"aap\">" +
            "  <!-- hoepla\n" +
            "-->\n" +
            "  <sv:property sv:name=\"mies\">" +
            "    <sv:value>noot</sv:value>" +
            "  </sv:property>" +
            "</sv:node>");
    }

    @Test
    public void testEntitiesEscaped() throws IOException, ParserConfigurationException, SAXException {
        assertFormat(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" sv:name=\"aap\">\n" +
            "  <sv:property sv:name=\"mies\">\n" +
            "    <sv:value>a &amp; b &gt; c</sv:value>\n"+
            "  </sv:property>\n" +
            "</sv:node>\n", 
            
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" sv:name=\"aap\">" +
            "<sv:property sv:name=\"mies\">" +
            "<sv:value>a &amp; b &gt; c</sv:value>" +
            "</sv:property>"+
            "</sv:node>\n");
    }

    @Test
    public void whiteSpaceInInputIsIgnored() throws IOException, ParserConfigurationException, SAXException {
        assertFormat(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" sv:name=\"aap\">\n" +
            "  <sv:property sv:name=\"mies\">\n" +
            "    <sv:value>bla</sv:value>\n"+
            "  </sv:property>\n" +
            "</sv:node>\n",

            "<?xml version=\"1.0\"?>\n"+
            "<sv:node xmlns:sv=\"jcr-sv-namespace\" sv:name=\"aap\">\n"+
            "  <sv:property sv:name=\"mies\">\n"+
            "    <sv:value>bla</sv:value>\n"+
            "  </sv:property>\n"+
            "</sv:node>");
    }

}
