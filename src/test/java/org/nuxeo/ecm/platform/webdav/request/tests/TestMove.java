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

import javax.servlet.ServletException;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;

public class TestMove extends AbstractWebDavRequestTestCase {

    public TestMove(String name) {
        super(name);
    }

    public void testRename() throws IOException, ServletException,
            ClientException {
        URLResolverCache.resetCache();

        FakeRequest fReq = new FakeRequest("MOVE",
                "/demo/Folder1/Folder2/Folder3");
        fReq.addHeader("Destination",
                "http://127.0.0.1:8080/nuxeo/dav/demo/Folder1/Folder2/Folder4");
        fReq.addHeader("Overwrite", "T");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_NO_CONTENT, res.getStatus());

        folder3 = remote.getChildren(folder2.getRef(), "Folder").get(0);
        assertEquals("Folder4", folder3.getTitle());
    }

    public void testRenameOnCrappyClient() throws IOException, ServletException,
            ClientException {
        URLResolverCache.resetCache();

        FakeRequest fReq = new FakeRequest("MOVE","/demo/Folder1/Folder2/Folder3?displayName=/Bitou");
        fReq.addHeader("Destination",
                "http://127.0.0.1:8080/nuxeo/dav/demo/Folder1/Folder2/Folder3?displayName=Bitou2");
        fReq.addHeader("Overwrite", "T");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_NO_CONTENT, res.getStatus());

        folder3 = remote.getChildren(folder2.getRef(), "Folder").get(0);
        assertEquals("Bitou2", folder3.getTitle());
    }

    public void testMoveFile() throws IOException, ServletException,
            ClientException {
        URLResolverCache.resetCache();

        FakeRequest fReq = new FakeRequest("MOVE",
                "/demo/Folder1/Folder2/Folder3/testBlob.txt");
        fReq.addHeader("Destination",
                "http://127.0.0.1:8080/nuxeo/dav/demo/Folder1/Folder2/testBlob.txt");
        fReq.addHeader("Overwrite", "T");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_CREATED, res.getStatus());

        file = remote.getChildren(folder2.getRef(), "File").get(0);
        assertNotNull(file);
        assertEquals("File1", file.getTitle());
    }

}
