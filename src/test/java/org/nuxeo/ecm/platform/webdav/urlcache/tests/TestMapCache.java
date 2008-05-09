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

package org.nuxeo.ecm.platform.webdav.urlcache.tests;

import junit.framework.TestCase;

import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.webdav.urlcache.URLCacheBackend;

public class TestMapCache extends TestCase {

    public void testMaxSize() {
        URLCacheBackend map = new URLCacheBackend(5, true);

        for (int i = 1; i < 10; i++) {
            map.put("/k" + i, new IdRef("ref" + i));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        assertTrue(map.keySet().size() <= 5);
    }

    public void testOrder() {
        URLCacheBackend map = new URLCacheBackend(5, true);

        for (int i = 1; i < 10; i++) {
            map.put("/k" + i, new IdRef("ref" + i));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            map.get("k1");
        }

        assertTrue(map.keySet().size() <= 5);
        assertTrue(map.keySet().contains("/k1"));
        assertTrue(!map.keySet().contains("/k2"));
    }

    public void testMatchingEntries() {
        URLCacheBackend map = new URLCacheBackend(10, true);

        for (int i = 0; i < 10; i++) {
            map.put("k" + i, new IdRef("ref" + i % 2));
        }

        assertEquals(5, map.getMachingEntries(new IdRef("ref1")).size());
        assertEquals(5, map.getMachingEntries(new IdRef("ref0")).size());
    }

}
