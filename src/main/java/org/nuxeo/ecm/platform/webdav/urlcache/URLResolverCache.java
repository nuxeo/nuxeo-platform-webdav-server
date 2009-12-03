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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.platform.webdav.servlet.NuxeoWebDavServlet;

/**
 * Cache for URL => document resolution.
 * <p>
 * This cache is useful because URL resolution may be costly.
 *
 * @author tiry
 */
public class URLResolverCache {

    private static final int CACHE_SIZE = 10000;

    private static final URLCacheBackend cache = new URLCacheBackend(CACHE_SIZE);

    private static final Log log = LogFactory.getLog(URLResolverCache.class);

    // Utility class.
    private URLResolverCache() {
    }

    public static void addToCache(String url, DocumentModel doc) {
        String urlkey;
        if (url.startsWith("http://") || url.startsWith("https://")) {
            int idx = url.indexOf(NuxeoWebDavServlet.PATTERN);
            urlkey = url.substring(idx + NuxeoWebDavServlet.PATTERN.length());
        } else {
            urlkey = url;
        }

        cache.put(urlkey, doc);
        addAlternativeURLs(urlkey, doc);
    }

    public static void addToCache(DocumentModel container, String vPath,
            DocumentModel doc) {
        String urlkey = container.getRepositoryName()
                + container.getPathAsString();

        if (urlkey.endsWith("/")) {
            urlkey += vPath;
        } else {
            urlkey += '/' + vPath;
        }

        cache.put(urlkey, doc);
        addAlternativeURLs(urlkey, doc);
    }

    /**
     * Adds alternative URLs based on the registered parent URLs.
     */
    private static void addAlternativeURLs(String url, DocumentModel doc) {
        DocumentRef parentRef = doc.getParentRef();
        List<String> parentUrls = cache.getMachingEntries(parentRef);

        String[] pathParts = url.split("/");

        String childVirtualPath = pathParts[pathParts.length - 1];

        for (String parentURL : parentUrls) {
            String newVPath = parentURL;
            if (!newVPath.endsWith("/")) {
                newVPath += "/";
            }
            newVPath += childVirtualPath;
            cache.put(newVPath, doc);
        }
    }

    public static void removeFromCache(DocumentModel doc) {
        cache.remove(doc);
    }

    public static DocumentRef resolveUrlFromCache(String url) {
        String urlkey = null;
        // normalize url
        if (url.startsWith("/")) {
            urlkey = url;
        } else {
            urlkey = '/' + url;
        }
        DocumentRef ref = cache.get(urlkey);

        if (ref == null) {
            // try resolution via a direct child
            for (String k : cache.keySet()) {
                if (isDirectChild(urlkey, k)) {
                    return cache.getParent(k);
                }
            }
        }
        return ref;
    }

    private static boolean isDirectChild(String parentPath, String childPath) {
        if (!childPath.startsWith(parentPath)) {
            return false;
        }

        parentPath = cache.getNormalizedKey(parentPath);
        childPath = cache.getNormalizedKey(childPath);

        return parentPath.split("/").length + 1 == childPath.split("/").length;
    }

    public static DocumentRef resolveUrlFromCache(String repo, String url) {
        String urlkey;
        if (url.startsWith("/")) {
            urlkey = '/' + repo + url;
        } else {
            urlkey = '/' + repo + '/' + url;
        }
        return resolveUrlFromCache(urlkey);
    }

    public static void resetCache() {
        cache.reset();
    }

}
