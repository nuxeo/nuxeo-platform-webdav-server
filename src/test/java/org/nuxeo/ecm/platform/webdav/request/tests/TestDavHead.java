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

import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;

public class TestDavHead extends AbstractWebDavRequestTestCase {

    public TestDavHead(String name) {
        super(name);
    }

    public void testDoHead1() throws ServletException, IOException {
        URLResolverCache.resetCache();

        FakeResponse res;

        res = execDavRequest("HEAD", "/demo/folder-1/folder-2/folder-3/");
        assertEquals(200, res.getStatus());

        res = execDavRequest("HEAD", "/demo/Folder1/Folder2/Folder3/foo/bar/");
        assertEquals(404, res.getStatus());

        res = execDavRequest("HEAD", "/demo/folder-1/folder-2/Folder3/");
        assertEquals(200, res.getStatus());
    }

    public void testDoHead2() throws ServletException, IOException {
        URLResolverCache.resetCache();

        FakeResponse res = execDavRequest("HEAD",
                "/demo/folder-1/folder-2/Folder3");
        assertEquals(200, res.getStatus());

        res = execDavRequest("HEAD", "/demo/folder-1/folder-2/Folder3/");
        assertEquals(200, res.getStatus());
    }

    public void testDoHead3() throws ServletException, IOException {
        URLResolverCache.resetCache();

        FakeResponse res = execDavRequest("HEAD",
                "/demo/folder-1/folder-2/Folder3/note-1");
        assertEquals(200, res.getStatus());

        res = execDavRequest("HEAD",
                "/demo/folder-1/folder-2/Folder3/Note1.txt");
        assertEquals(200, res.getStatus());

        res = execDavRequest("HEAD", "/demo/folder-1/folder-2/Folder3/file-1");
        assertEquals(200, res.getStatus());

        res = execDavRequest("HEAD",
                "/demo/folder-1/folder-2/Folder3/testBlob.txt");
        assertEquals(200, res.getStatus());
    }

}
