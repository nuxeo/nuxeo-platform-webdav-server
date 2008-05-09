/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.webdav.mapping;

import java.io.IOException;
import java.util.List;

import org.dom4j.Element;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.SingleDocumentReader;



/**
 * Helper to extract Nuxeo complex properties as XML.
 * <p>
 * (not used for now)
 * @author tiry
 */
public class NuxeoComplexTypeExtractor {

    private final DocumentModel doc;

    private ExportedDocument cachedXmlDoc;

    public NuxeoComplexTypeExtractor(DocumentModel doc) {
        this.doc = doc;
    }

    public Element extractProperty(String schemaName, String propertyName) {

        ExportedDocument xmlDoc = null;
        try {
            xmlDoc = getXMLView();
        } catch (IOException e) {
            return null;
        }

        Element targetSchema = null;

        for (Element schema : (List<Element>) xmlDoc.getDocument().getRootElement().elements(
                "schema")) {
            if (schemaName.equals(schema.attribute("name").getValue())) {
                targetSchema = schema;
                break;
            }
        }

        if (targetSchema == null) {
            return null;
        }

        Element field = targetSchema.element(propertyName);

        if (field != null) {
            return field;
        }
        return null;
    }

    private ExportedDocument getXMLView() throws IOException {
        if (cachedXmlDoc == null) {
            DocumentReader reader = new SingleDocumentReader(getSession(doc),
                    doc);
            cachedXmlDoc = reader.read();
        }
        return cachedXmlDoc;
    }

    private static CoreSession getSession(DocumentModel doc) {
        return CoreInstance.getInstance().getSession(doc.getSessionId());
    }

}
