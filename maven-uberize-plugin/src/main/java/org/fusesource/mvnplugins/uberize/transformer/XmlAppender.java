package org.fusesource.mvnplugins.uberize.transformer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.fusesource.mvnplugins.uberize.UberEntry;
import org.fusesource.mvnplugins.uberize.Uberizer;
import org.codehaus.plexus.util.IOUtil;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class XmlAppender extends AbstractPathTransformer {
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    protected UberEntry process(Uberizer uberizer, UberEntry entry, File target) throws IOException {
        Document doc=null;
        OutputStream out = new FileOutputStream(target);
        try {
            for (File source : entry.getSources()) {
                doc = merge(doc, source);
            }
            new XMLOutputter(Format.getPrettyFormat()).output(doc, out);
        } finally {
            IOUtil.close(out);
        }
        return new UberEntry(entry).addSource(target);
    }

    private Document merge(Document doc, File source) throws IOException {
        InputStream in = new FileInputStream(source);
        try {
            Document sourceDoc;
            try {
                sourceDoc = new SAXBuilder().build(in);
            }
            catch (JDOMException e) {
                throw new RuntimeException(e);
            }

            if (doc == null) {
                doc = sourceDoc;
            } else {
                Element root = sourceDoc.getRootElement();

                for (Iterator itr = root.getAttributes().iterator(); itr.hasNext();) {
                    Attribute a = (Attribute) itr.next();
                    itr.remove();

                    Element mergedEl = doc.getRootElement();
                    Attribute mergedAtt = mergedEl.getAttribute(a.getName(), a.getNamespace());
                    if (mergedAtt == null) {
                        mergedEl.setAttribute(a);
                    }
                }

                for (Iterator itr = root.getChildren().iterator(); itr.hasNext();) {
                    Content n = (Content) itr.next();
                    itr.remove();

                    doc.getRootElement().addContent(n);
                }
            }


        } finally {
            IOUtil.close(in);
        }
        return doc;
    }
}
