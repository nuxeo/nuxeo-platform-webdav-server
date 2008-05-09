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

package org.nuxeo.ecm.platform.webdav.urlcache;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;

/**
 * Cache entry for URL=>Document resolution.
 *
 * @author tiry
 */
public class URLCacheEntry {

    protected final DocumentRef ref;

    protected DocumentRef parentRef;

    protected long timeStamp;

    public URLCacheEntry(DocumentRef ref) {
        this.ref = ref;
        timeStamp = System.currentTimeMillis();
    }

    public URLCacheEntry(DocumentModel doc) {
        ref = doc.getRef();
        parentRef = doc.getParentRef();
        timeStamp = System.currentTimeMillis();
    }

    public void updateTimeStamp() {
        timeStamp = System.currentTimeMillis();
    }

    public DocumentRef getRef() {
        return ref;
    }

    public DocumentRef getParentRef() {
        return parentRef;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

}
