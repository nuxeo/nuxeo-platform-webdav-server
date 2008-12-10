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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;


/**
 * URLCache storage back-end.
 * <p>
 * Uses memory with an integrated GC.
 *
 * @author tiry
 */
public class URLCacheBackend {

    protected static final int GC_RATIO = 4;

    protected int maxSize = 5000;

    protected long oldestEntry;

    protected long newestEntry;

    protected final Map<String, URLCacheEntry> data = new HashMap<String, URLCacheEntry>();

    protected boolean forceRecusiveGC = false;

    public URLCacheBackend(int maxSize, boolean recursiveGC) {
        this.maxSize = maxSize;
        forceRecusiveGC = recursiveGC;
        oldestEntry = System.currentTimeMillis();
        newestEntry = 0;
    }

    public URLCacheBackend(int maxSize) {
        this(maxSize, false);
    }

    public void reset() {
        data.clear();
        oldestEntry = System.currentTimeMillis();
        newestEntry = 0;
    }

    @Deprecated
    public void put(String key, DocumentRef docRef) {
        String storeKey = getNormalizedKey(key);
        if (data.containsKey(storeKey)) {
            data.get(storeKey).updateTimeStamp();

        } else {
            gcIfNeeded();
            synchronized (data) {
                data.put(storeKey, new URLCacheEntry(docRef));
            }
        }
        newestEntry = System.currentTimeMillis();
    }

    public static String getNormalizedKey(String key) {
        String storeKey;
        if (key.endsWith("/")) {
            storeKey = key.substring(0, key.length() - 1);
        } else {
            storeKey = key;
        }

        if (!storeKey.startsWith("/")) {
            storeKey = '/' + storeKey;
        }

        return storeKey;
    }

    public void put(String key, DocumentModel doc) {
        String storeKey = getNormalizedKey(key);

        if (data.containsKey(storeKey)) {
            data.get(storeKey).updateTimeStamp();
        } else {
            gcIfNeeded();
            synchronized (data) {
                data.put(storeKey, new URLCacheEntry(doc));
            }
        }
        newestEntry = System.currentTimeMillis();
    }

    public DocumentRef get(String key) {
        String storeKey = getNormalizedKey(key);

        if (data.containsKey(storeKey)) {
            URLCacheEntry entry = data.get(storeKey);
            entry.updateTimeStamp();
            newestEntry = entry.getTimeStamp();
            return entry.getRef();
        } else {
            return null;
        }
    }

    public void remove(String key) {
        String storeKey = getNormalizedKey(key);
        data.remove(storeKey);
    }

    public DocumentRef getParent(String key) {
        String storeKey = getNormalizedKey(key);

        if (data.containsKey(storeKey)) {
            URLCacheEntry entry = data.get(storeKey);
            entry.updateTimeStamp();
            newestEntry = entry.getTimeStamp();
            return entry.getParentRef();
        } else {
            return null;
        }
    }

    public Set<String> keySet() {
        return data.keySet();
    }

    public List<String> getMachingEntries(DocumentRef docRef) {
        List<String> urls = new ArrayList<String>();

        for (String url : data.keySet()) {
            DocumentRef ref = data.get(url).getRef();
            if (ref.equals(docRef)) {
                urls.add(url);
            }
        }
        return urls;
    }

    public void remove(DocumentModel doc) {
        DocumentRef docRef = doc.getRef();
        List<String> keysToDelete = new LinkedList<String>();
        for (String key : data.keySet()) {
            if (data.get(key).getRef().equals(docRef)) {
                keysToDelete.add(key);
            }
        }

        deleteKeys(keysToDelete);
    }

    private void gcIfNeeded() {
        if (data.keySet().size() < maxSize) {
            return;
        }

        long tsRange = newestEntry - oldestEntry;

        long maxTs = oldestEntry + tsRange / GC_RATIO;

        List<String> keysToDelete = new LinkedList<String>();

        for (String key : data.keySet()) {
            if (data.get(key).getTimeStamp() < maxTs) {
                keysToDelete.add(key);
            }
        }

        deleteKeys(keysToDelete);

        oldestEntry = maxTs;

        if (forceRecusiveGC && data.keySet().size() > maxSize) {
            gcIfNeeded();
        }
    }

    private void deleteKeys(List<String> keysToDelete) {
        synchronized (data) {
            for (String key : keysToDelete) {
                data.remove(key);
            }
        }
    }

}
