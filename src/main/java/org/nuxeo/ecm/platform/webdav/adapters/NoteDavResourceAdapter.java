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

package org.nuxeo.ecm.platform.webdav.adapters;

import java.io.IOException;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavRequestWrapper;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavResponseWrapper;

/**
 * DAV Resource adapter for Note document type.
 *
 * @author tiry
 */
public class NoteDavResourceAdapter extends AbstractDavResourceAdapter {

    private String content;

    private String contentType;

    private int contentLength = -1;

    private String fileName;

    private void readFileAttributes() throws ClientException {
        if (content == null) {
            content = (String) doc.getProperty("note", "note");
            fileName = doc.getTitle();
            if (content == null) {
                contentType = "text/plain";
                contentLength = 0;
                fileName += ".txt";
            } else {
                contentLength = content.length();

                if (content.contains("<?xml")) {
                    contentType = "text/xml";
                    fileName += ".xml";
                } else if (content.contains("<html")) {
                    contentType = "text/html";
                    fileName += ".html";
                } else {
                    contentType = "text/plain";
                    fileName += ".txt";
                }
            }
        }
    }

    @Override
    public void doGet(WebDavRequestWrapper req, WebDavResponseWrapper res) throws ClientException {
          readFileAttributes();
          res.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\";");
          res.setHeader("Last-Modified", getMofificationDate());
          res.setContentType(contentType);
          res.setContentLength(contentLength);
          try {
              res.getWriter().print(content);
              //res.flushBuffer();
          } catch (IOException e) {
              // XXX: better throw the exception instead of hiding the
              // root cause of a potential error
              //log.error("Error while downloading the file: " + filename);
          }
    }

    @Override
    public long getContentLength() throws ClientException {
        readFileAttributes();
        return contentLength;
    }

    @Override
    public String getContentType() throws ClientException {
        readFileAttributes();
        return contentType;
    }

    @Override
    public String getFileName() throws ClientException {
        readFileAttributes();
        return fileName;
    }

}
