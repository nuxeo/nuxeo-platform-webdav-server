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

public class TestDavPUT extends AbstractWebDavRequestTestCase {

    public TestDavPUT(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deploy("nxfilemanager-service.xml");
        deploy("nxfilemanager-plugins-contrib.xml");
        deploy("nxmimetype-service.xml");
        deploy("nxtypes-framework.xml");
        deploy("ecm-types-contrib.xml");
        URLResolverCache.resetCache();
    }

    public void testPut() throws IOException, ServletException, ClientException {
        FakeRequest fReq = new FakeRequest("PUT",
                "/demo/folder-1/folder-2/testPut.txt");

        String data = "SomeDummyData";
        FakeServletInputStream in = new FakeServletInputStream(data);
        fReq.setStream(in);
        fReq.setContentType("text/plain");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_CREATED, res.getStatus());

        assertTrue(!remote.getChildren(folder2.getRef(), "Note").isEmpty());
    }

    public void testPutVPath() throws IOException, ServletException,
            ClientException {
        FakeRequest fReq = new FakeRequest("PUT",
                "/demo/Folder1/Folder2/testPut.txt");

        String data = "SomeDummyData";
        FakeServletInputStream in = new FakeServletInputStream(data);
        fReq.setStream(in);
        fReq.setContentType("text/plain");
        fReq.addHeader("needGetParameterForCollectionNamming", "T");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_CREATED, res.getStatus());

        assertTrue(!remote.getChildren(folder2.getRef(), "Note").isEmpty());
    }

}
