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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;

public class TestMKCol extends AbstractWebDavRequestTestCase {

    public TestMKCol(String name) {
        super(name);
    }

    public void testMKCol() throws IOException, ServletException,
            ClientException {
        URLResolverCache.resetCache();

        FakeRequest fReq = new FakeRequest("MKCOL",
                "/demo/Folder1/Folder2/Folder3/Folder4");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_CREATED, res.getStatus());

        DocumentModel folder4 = remote.getChildren(folder3.getRef(), "Folder").get(
                0);
        assertEquals("Folder4", folder4.getTitle());
    }

    public void testMKColWithSlash() throws IOException, ServletException,
            ClientException {
        URLResolverCache.resetCache();

        FakeRequest fReq = new FakeRequest("MKCOL",
                "/demo/Folder1/Folder2/Folder3/Folder4/");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_CREATED, res.getStatus());

        DocumentModel folder4 = remote.getChildren(folder3.getRef(), "Folder").get(
                0);
        assertEquals("Folder4", folder4.getTitle());
    }

    public void testNewFolder() throws IOException, ServletException,
            ClientException {
        URLResolverCache.resetCache();

        FakeRequest fReq = new FakeRequest("MKCOL",
                "/demo/Folder1/Folder2/Folder3/New Folder");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_CREATED, res.getStatus());

        DocumentModel folder4 = remote.getChildren(folder3.getRef(), "Folder").get(
                0);
        assertEquals("New Folder", folder4.getTitle());
    }

    public void testMKColConflict() throws IOException, ServletException,
            ClientException {
        URLResolverCache.resetCache();

        FakeRequest fReq = new FakeRequest("MKCOL",
                "/demo/Folder1/Folder2/Folder3/Folder4/Folder5");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_CONFLICT, res.getStatus());
    }

    public void testNewFolderAndRename() throws IOException, ServletException,
            ClientException {
        URLResolverCache.resetCache();

        FakeRequest fReq = new FakeRequest("MKCOL",
                "/demo/Folder1/Folder2/Folder3/New Folder");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_CREATED, res.getStatus());

        DocumentModel folder4 = remote.getChildren(folder3.getRef(), "Folder").get(
                0);
        assertEquals("New Folder", folder4.getTitle());

        fReq = new FakeRequest("MOVE",
                "/demo/Folder1/Folder2/Folder3/New Folder");
        fReq.addHeader("Destination",
                "http://127.0.0.1:8080/nuxeo/dav/demo/Folder1/Folder2/Folder3/MyFolder");
        fReq.addHeader("Overwrite", "T");

        res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_NO_CONTENT, res.getStatus());

        folder4 = remote.getChildren(folder3.getRef(), "Folder").get(0);
        assertEquals("MyFolder", folder4.getTitle());
    }

}
