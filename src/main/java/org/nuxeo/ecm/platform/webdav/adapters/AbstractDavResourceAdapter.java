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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PagedDocumentsProvider;
import org.nuxeo.ecm.core.search.api.client.query.QueryException;
import org.nuxeo.ecm.core.search.api.client.querymodel.QueryModel;
import org.nuxeo.ecm.core.search.api.client.querymodel.QueryModelService;
import org.nuxeo.ecm.core.search.api.client.querymodel.descriptor.QueryModelDescriptor;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavRequestWrapper;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavResponseWrapper;
import org.nuxeo.runtime.api.Framework;

/**
 * Base class for DAV resources adapters.
 * 
 * @author tiry
 */
public abstract class AbstractDavResourceAdapter implements DavResourceAdapter {

    protected DocumentModel doc;

    protected String qmName;

    protected final List<String> parameters = new ArrayList<String>();

    private CoreSession session;

    private String lastModified;

    public void setDocumentModel(DocumentModel doc) {
        this.doc = doc;
    }

    protected String getMofificationDate() throws ClientException {
        if (lastModified == null) {
            Calendar modified = (Calendar) doc.getProperty("dublincore",
                    "modified");
            if (modified == null) {
                lastModified = WebDavConst.HTTP_HEADER_DATE_FORMAT.format(new Date());
            } else {
                lastModified = WebDavConst.HTTP_HEADER_DATE_FORMAT.format(modified.getTime());
            }
        }
        return lastModified;
    }

    protected CoreSession getSession() {
        if (session == null) {
            if (doc == null) {
                return null;
            }
            String sid = doc.getSessionId();
            session = CoreInstance.getInstance().getSession(sid);
        }

        return session;
    }

    protected PagedDocumentsProvider execQueryModel() throws ClientException,
            QueryException {
        QueryModelService qmService = (QueryModelService) Framework.getRuntime().getComponent(
                QueryModelService.NAME);
        QueryModelDescriptor qmd = qmService.getQueryModelDescriptor(qmName);

        QueryModel qm = new QueryModel(qmd);
        return qm.getResultsProvider(getSession(), parameters.toArray());
    }

    public String getURLPart(String data) {
        PagedDocumentsProvider provider;
        try {
            provider = execQueryModel();
        } catch (Exception e) {
            return data;
        }

        DocumentModelList dml = provider.getCurrentPage();
        if (dml.size() == 1) {
            return data;
        } else {
            // find the document order index
            int idx = 0;
            for (DocumentModel dm : dml) {
                if (dm.getRef().equals(doc.getRef())) {
                    return encodeOrderedTitle(data, idx);
                }
                idx++;
            }

            return data;
        }
    }

    protected static String encodeOrderedTitle(String title, int idx) {
        return title + '(' + idx + ')';
    }

    public void doGet(WebDavRequestWrapper req, WebDavResponseWrapper res)
            throws IOException, ClientException {
    }

    public long getContentLength() throws ClientException {
        return 0;
    }

    public String getContentType() throws ClientException {
        return null;
    }

    public String getFileName() throws ClientException {
        return null;
    }

    public void rename(String title) throws ClientException {
        // Minimal implementation
        doc.setProperty("dublincore", "title", title);
    }

}
