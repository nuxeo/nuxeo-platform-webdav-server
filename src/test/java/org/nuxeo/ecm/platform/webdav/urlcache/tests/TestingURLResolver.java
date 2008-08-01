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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.platform.webdav.adapters.DavResourceAdapter;
import org.nuxeo.ecm.platform.webdav.helpers.CoreHelper;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;

public class TestingURLResolver extends AbstractWebDavTestCaseWithRepository {

    public TestingURLResolver(String name) {
        super(name);
    }

    public void testFoldersResolution() {
        URLResolverCache.resetCache();

        assertEquals("/folder-1/folder-2/folder-3", folder3.getPathAsString());

        // check resolution
        DocumentModel resolved1 = CoreHelper.resolveVirtualPath(remote,
                "/Folder1", null);
        assertNotNull(resolved1);
        assertEquals(resolved1.getRef(), folder1.getRef());

        // check cache update
        DocumentRef resolvedRef1 = URLResolverCache.resolveUrlFromCache(
                resolved1.getRepositoryName(), "/Folder1");
        assertNotNull(resolvedRef1);
        assertEquals(resolvedRef1, folder1.getRef());

        // check resolution
        DocumentModel resolved3 = CoreHelper.resolveVirtualPath(remote,
                "/Folder1/Folder2/Folder3", null);
        assertNotNull(resolved3);
        assertEquals(resolved3.getRef(), folder3.getRef());

        // check cache update
        DocumentRef resolvedRef3 = URLResolverCache.resolveUrlFromCache(
                resolved1.getRepositoryName(), "/Folder1/Folder2/Folder3");
        assertNotNull(resolvedRef3);
        assertEquals(resolvedRef3, folder3.getRef());

        DocumentRef resolvedRef2 = URLResolverCache.resolveUrlFromCache(
                resolved1.getRepositoryName(), "/Folder1/Folder2");
        assertNotNull(resolvedRef2);
        assertEquals(resolvedRef2, folder2.getRef());

        // check cache with mixed up URLs
        assertNotNull(URLResolverCache.resolveUrlFromCache(
                resolved1.getRepositoryName(), "/folder-1/Folder2/Folder3"));
        assertNotNull(URLResolverCache.resolveUrlFromCache(
                resolved1.getRepositoryName(), "/folder-1/folder-2/Folder3"));
        // assertNotNull(URLResolverCache.resolveUrlFromCache(resolved1.getRepositoryName(),
        // "/Folder1/folder-2/Folder3")); => no use case !
    }

    public void testFileResolution() {

        URLResolverCache.resetCache();
        assertEquals("/folder-1/folder-2/folder-3/file-1", file.getPathAsString());

        DavResourceAdapter adapter = file.getAdapter(DavResourceAdapter.class);
        assertNotNull(adapter);
        assertEquals("testBlob.txt", adapter.getFileName());

        // check resolution
        DocumentModel resolved1 = CoreHelper.resolveVirtualPath(remote,
                "/Folder1/Folder2/Folder3/testBlob.txt", null);
        assertNotNull(resolved1);
        assertEquals(resolved1.getRef(), file.getRef());

        DocumentRef resolvedRef = URLResolverCache.resolveUrlFromCache(
                resolved1.getRepositoryName(),
                "/Folder1/Folder2/Folder3/testBlob.txt");
        assertNotNull(resolvedRef);
        assertEquals(resolvedRef, file.getRef());

        resolvedRef = URLResolverCache.resolveUrlFromCache(
                resolved1.getRepositoryName(),
                "/folder-1/Folder2/Folder3/testBlob.txt");
        assertNotNull(resolvedRef);
        assertEquals(resolvedRef, file.getRef());

        resolvedRef = URLResolverCache.resolveUrlFromCache(
                resolved1.getRepositoryName(),
                "/folder-1/folder-2/Folder3/testBlob.txt");
        assertNotNull(resolvedRef);
        assertEquals(resolvedRef, file.getRef());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Testing WebDav Url Resolver");

        suite.addTest(new TestingURLResolver("testFoldersResolution"));
        suite.addTest(new TestingURLResolver("testFileResolution"));
        return suite;
    }

}
