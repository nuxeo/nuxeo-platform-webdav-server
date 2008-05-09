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
 * $Id: JOOoConvertPluginImpl.java 18651 2007-05-13 20:28:53Z sfermigier $
 */

package org.nuxeo.ecm.platform.webdav.adapters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.SingleDocumentReader;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDocumentWriter;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavRequestWrapper;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavResponseWrapper;

/**
 * Implements WebDAV GET as an XML export.
 *
 * @author tiry
 */
public class DefaultNonFolderishDavDownloadAdapter
        extends AbstractDavResourceAdapter {

    private String export;


    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public long getContentLength() {
        createExport();
        return export.length();
    }

    @Override
    public String getFileName() {
        return doc.getTitle();
    }

    @Override
    public void doGet(WebDavRequestWrapper req, WebDavResponseWrapper res) {
        try {
            createExport();
            res.setContentLength(export.length());
            res.getWriter().write(export);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        res.setContentType("text/xml");
        res.setHeader("Last-Modified", getMofificationDate());
    }

    private void createExport() {
        if (export != null) {
            return;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DocumentReader reader = new SingleDocumentReader(getSession(doc), doc);
        DocumentWriter writer = new XMLDocumentWriter(out);

        DocumentPipe pipe = new DocumentPipeImpl();
        pipe.setReader(reader);
        pipe.setWriter(writer);
        try {
            pipe.run();
        } catch (Exception e) {
            // XXX !
            export = "<error>" + e.getMessage() + "</error>";
            return;
        }

        export = out.toString();
    }

    private static CoreSession getSession(DocumentModel doc) {
        return CoreInstance.getInstance().getSession(doc.getSessionId());
    }

}
