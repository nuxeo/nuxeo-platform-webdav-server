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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;
import org.nuxeo.ecm.platform.webdav.config.WebDavConfigurationService;
import org.nuxeo.runtime.api.Framework;

/**
 * Factory for DAV resources adapters.
 * <p>
 * Uses an extension point to find the adapter
 * implementation according to document type.
 *
 * @author tiry
 */
public class DavResourceAdapterFactory implements DocumentAdapterFactory {

    private static WebDavConfigurationService configService;

    private static final Log log = LogFactory.getLog(DavResourceAdapterFactory.class);

    private static WebDavConfigurationService getConfigService() {
        if (configService == null) {
            configService = (WebDavConfigurationService) Framework.getRuntime().getComponent(
                    WebDavConfigurationService.NAME);
        }
        return configService;
    }

    public Object getAdapter(DocumentModel doc, Class cls) {

        DavResourceAdapter adapter = null;

        // first try to get Type Adapter
        try {
            adapter = getConfigService().getAdapterForType(doc.getType());
        } catch (InstantiationException e) {
            log.error("Error while getting DAV adapter for type "
                    + doc.getType() + ':' + e.getMessage());
        } catch (IllegalAccessException e) {
            log.error("Error while getting DAV adapter for type "
                    + doc.getType() + ':' + e.getMessage());
        }

        if (adapter == null) {
            // use default built-in adapters
            if (doc.isFolder()) {
                adapter =  new DefaultFolderishDavDownloadAdapter();
            } else {
                if (doc.hasSchema("file")) {
                    adapter =  new FileBasedDavResourceAdapter();
                }
            /*    else if (doc.getType().equals("Note"))
                {
                    adapter =  new NoteDavResourceAdapter();
                }*/
                else {
                    adapter =  new DefaultNonFolderishDavDownloadAdapter();
                }
            }
        }

        adapter.setDocumentModel(doc);
        return adapter;
    }

}
