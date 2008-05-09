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

import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavRequestWrapper;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavResponseWrapper;

/**
 * Implements WebDav GET as a file download.
 *
 * @author tiry
 */
public class FileBasedDavResourceAdapter extends AbstractDavResourceAdapter {

    private Blob blob;

    private String contentType;

    private long contentLength = -1;

    private String fileName;

    private String title;

    private String extension;

    private void readFileAttributes() {
        if (blob == null) {
            blob = (Blob) doc.getProperty("file", "content");
            contentType = blob.getMimeType();
            contentLength = blob.getLength();
            if (contentLength == -1) {
                contentLength = 100;
            }
            fileName = (String) doc.getProperty("file", "filename");
            title = doc.getTitle();

            String[] fileNameParts = fileName.split("\\.");
            if (fileNameParts.length > 1) {
                extension = fileNameParts[fileNameParts.length - 1];
            } else {
                extension = "";
            }
        }
    }

    @Override
    public String getContentType() {
        readFileAttributes();
        return contentType;
    }

    @Override
    public long getContentLength() {
        readFileAttributes();
        return contentLength;
    }

    @Override
    public String getFileName() {
        readFileAttributes();
        return fileName;
    }

    @Override
    public void doGet(WebDavRequestWrapper req, WebDavResponseWrapper res) {
        Blob blob = (Blob) doc.getProperty("file", "content");
        String fileName = (String) doc.getProperty("file", "filename");

        res.setHeader("Content-Disposition", "inline; filename=\""
                + fileName + "\";");
        res.setContentType(blob.getMimeType());
        res.setHeader("Last-Modified", getMofificationDate());
        res.setContentType(contentType);
        res.setContentLength(Integer.parseInt(Long.toString(contentLength)));

        try {
            blob.transferTo(res.getOutputStream());
            //res.flushBuffer();
        } catch (IOException e) {
            // XXX: better throw the exception instead of hiding the
            // root cause of a potential error
            //log.error("Error while downloading the file: " + filename);
        }
    }

    @Override
    public void rename(String title) {
    	doc.setProperty("file", "filename", title);
    	super.rename( getFileNameWithoutExtension(title) );
    }

    private String getFileNameWithoutExtension(String fileName) {
        Path path = new Path(fileName);
        return path.removeFileExtension().toString();
    }

}
