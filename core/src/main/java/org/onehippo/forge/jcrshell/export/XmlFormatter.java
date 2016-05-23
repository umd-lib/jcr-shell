package org.onehippo.forge.jcrshell.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public final class XmlFormatter {

    // private constructor for utility class
    private XmlFormatter() {
    }

    public static void format(File in, File out) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        InputStream istream = new FileInputStream(in);
        OutputStream ostream = null;
        OutputProcessor processor;
        try {
            try {
                if (!out.exists()) {
                    ostream = new FileOutputStream(out);
                    processor = new OutputProcessor(factory, istream, ostream);
                } else {
                    XmlProcessor recordProcessor = new XmlProcessor(factory, out);
                    recordProcessor.process();
                    ostream = new FileOutputStream(out);
                    processor = new OutputProcessor(factory, recordProcessor, istream, ostream);
                }
                processor.process();
            } finally {
                if (ostream != null) {
                    ostream.close();
                }
            }
        } finally {
            istream.close();
        }
    }
    
}
