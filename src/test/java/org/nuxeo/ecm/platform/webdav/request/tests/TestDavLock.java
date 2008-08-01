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

package org.nuxeo.ecm.platform.webdav.request.tests;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.locking.adapters.LockableDocument;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;

public class TestDavLock extends AbstractWebDavRequestTestCase {

    public TestDavLock(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deployContrib("org.nuxeo.ecm.platform.webdav","OSGI-INF/LockAdapters.xml");
        URLResolverCache.resetCache();
    }

    public void testLock() throws ServletException, IOException,
            ClientException {
        InputStream in = getResource("xml-dumps/lockRequest.xml").openStream();
        FakeRequest fReq = new FakeRequest("LOCK",
                "/demo/Folder1/Folder2/Folder3/testBlob.txt", in);

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_OK, res.getStatus());

        file = remote.getChildren(folder3.getRef(), "File").get(0);

        String lock = remote.getLock(file.getRef());
        assertNotNull(lock);

        /*
         * Element xml = res.getXMLOutput(); int s =
         * xml.elements("response").size(); assertEquals(s, 3);
         */
    }

    public void testLocked() throws ServletException, IOException,
            ClientException {

        LockableDocument lockableDoc = file.getAdapter(LockableDocument.class);
        lockableDoc.lock("Bitou");

        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/lockRequest.xml").openStream();
        FakeRequest fReq = new FakeRequest("LOCK",
                "/demo/Folder1/Folder2/Folder3/testBlob.txt", in);

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_LOCKED, res.getStatus());
    }

    public void testLockedByMe() throws ServletException, IOException,
            ClientException {

        LockableDocument lockableDoc = file.getAdapter(LockableDocument.class);
        lockableDoc.lock("Administrator");

        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/lockRequest.xml").openStream();
        FakeRequest fReq = new FakeRequest("LOCK",
                "/demo/Folder1/Folder2/Folder3/testBlob.txt", in);

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_OK, res.getStatus());
    }

    public void testUnlock() throws ServletException, IOException,
            ClientException {

        LockableDocument lockableDoc = file.getAdapter(LockableDocument.class);
        lockableDoc.lock("Administrator");

        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/lockRequest.xml").openStream();
        FakeRequest fReq = new FakeRequest("UNLOCK",
                "/demo/Folder1/Folder2/Folder3/testBlob.txt", in);

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_NO_CONTENT, res.getStatus());

        String lock = remote.getLock(file.getRef());
        assertNull(lock);
    }

}
