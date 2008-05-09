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

import java.io.IOException;
import java.util.Calendar;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavRequestWrapper;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavResponseWrapper;
import org.nuxeo.runtime.services.streaming.StreamSource;
import org.nuxeo.runtime.services.streaming.URLSource;

/**
 * Implements WebDav GET as an index.html generation.
 *
 * @author tiry
 */
public class DefaultFolderishDavDownloadAdapter extends
        AbstractDavResourceAdapter {

    private String index;

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public long getContentLength() {
        createIndex();
        return index.length();
    }

    @Override
    public String getFileName() {
        return doc.getTitle();
    }

    @Override
    public void doGet(WebDavRequestWrapper req, WebDavResponseWrapper res) {
        if (!req.getRequestURL().toString().endsWith("/")) {
            String goodURL = req.getRequestURL().append('/').toString();
            if (req.getQueryString() != null) {
                goodURL += '?' + req.getQueryString();
            }
            try {
                res.sendRedirect(goodURL);
                // res.flushBuffer();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            createIndex();
            res.getWriter().write(index);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        res.setContentType("text/html");
        res.setHeader("Last-Modified", getMofificationDate());
    }

    private void createIndex() {
        if (index != null) {
            return;
        }

        StreamSource shtml = new URLSource(
                DefaultFolderishDavDownloadAdapter.class.getResource("/templates/folder_contents.html"));
        String htmlTemplate;
        try {
            htmlTemplate = shtml.getString();
        } catch (IOException e) {
            htmlTemplate = "<html>TemplateError <br/> $CONTENTS</html>";
        }

        StringBuilder listingContent = new StringBuilder();

        CoreSession session = CoreInstance.getInstance().getSession(
                doc.getSessionId());

        DocumentModelList children;
        try {
            children = session.getChildren(doc.getRef());
        } catch (ClientException e) {
            return;
        }

        for (DocumentModel child : children) {
            listingContent.append("<tr><td>");

            listingContent.append("<a href=\"");
            listingContent.append(child.getName());
            listingContent.append("\"/>");
            listingContent.append(child.getTitle());
            listingContent.append("</a>");

            listingContent.append("</td><td>");

            listingContent.append(child.getType());

            listingContent.append("</td><td>");

            Calendar modified = (Calendar) child.getProperty("dublincore",
                    "modified");
            if (modified != null) {
                listingContent.append(modified.getTime().toGMTString());
            }

            listingContent.append("</td></tr>");
        }

        htmlTemplate = htmlTemplate.replace("$CONTENT",
                listingContent.toString());

        index = htmlTemplate;
    }

    /*public String getURLPart()
    {
        qmName = "SEARCH_DUPLICATED_TITLE";
        parameters.clear();
        parameters.add(doc.getType());
        parameters.add(doc.getPathAsString());
        parameters.add(doc.getTitle());
        return super.getURLPart(doc.getTitle());
    }*/

}
