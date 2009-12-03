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

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.platform.webdav.adapters.DavResourceAdapter;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;

public abstract class AbstractWebDavTestCaseWithRepository extends
        AbstractTestRepository {

    protected DocumentModel root;
    protected DocumentModel folder1;
    protected DocumentModel folder2;
    protected DocumentModel folder3;
    protected DocumentModel file;
    protected DocumentModel note;

    protected AbstractWebDavTestCaseWithRepository(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployContrib("org.nuxeo.ecm.platform.webdav", "OSGI-INF/dav-config-framework.xml");
        deployContrib("org.nuxeo.ecm.platform.webdav", "OSGI-INF/dav-config-contrib.xml");
        deployContrib("org.nuxeo.ecm.platform.webdav", "OSGI-INF/DavAdapters.xml");

        initialRepoSetup();
        URLResolverCache.resetCache();
    }

    public void initialRepoSetup() throws Exception {
        //DocumentModel root = getRootDocument();
        root = getRootDocument();

        DocumentModel doc = new DocumentModelImpl(root.getPathAsString(),
                "note", "Note");
        doc.setProperty("note", "note", "some Simple Text");

        doc = remote.createDocument(doc);
        DavResourceAdapter adapter = doc.getAdapter(DavResourceAdapter.class);
        assertNotNull(adapter);

        folder1 = new DocumentModelImpl(root.getPathAsString(), "folder-1",
                "Folder");
        folder1 = remote.createDocument(folder1);
        folder1.setProperty("dublincore", "title", "Folder1");
        remote.saveDocument(folder1);

        folder2 = new DocumentModelImpl(folder1.getPathAsString(), "folder-2",
                "Folder");
        folder2 = remote.createDocument(folder2);
        folder2.setProperty("dublincore", "title", "Folder2");
        remote.saveDocument(folder2);

        folder3 = new DocumentModelImpl(folder2.getPathAsString(), "folder-3",
                "Folder");
        folder3 = remote.createDocument(folder3);
        folder3.setProperty("dublincore", "title", "Folder3");
        remote.saveDocument(folder3);

        note = new DocumentModelImpl(folder3.getPathAsString(), "note-1",
                "Note");
        note = remote.createDocument(note);
        note.setProperty("dublincore", "title", "Note1");
        note.setProperty("note", "note", "Some simple plain text");
        remote.saveDocument(note);

        file = new DocumentModelImpl(folder3.getPathAsString(), "file-1",
                "File");
        file = remote.createDocument(file);

        file.setProperty("dublincore", "title", "File1");
        Blob blob = StreamingBlob.createFromString("Some text as file",
                "text/plain");
        file.setProperty("file", "content", blob);
        file.setProperty("file", "filename", "testBlob.txt");
        remote.saveDocument(file);

        remote.save();
    }

}
