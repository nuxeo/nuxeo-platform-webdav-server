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

import org.nuxeo.ecm.platform.webdav.servlet.NuxeoWebDavServlet;
import org.nuxeo.ecm.platform.webdav.urlcache.tests.AbstractWebDavTestCaseWithRepository;

/**
 * Test case base class for WebDAV Test cases.
 * <p>
 * Setup repository and documentModel adapters.
 * Provides helper methods to create fake WebDAV requests
 *
 * @author tiry
 */
public abstract class AbstractWebDavRequestTestCase extends
        AbstractWebDavTestCaseWithRepository {

    protected AbstractWebDavRequestTestCase(String name) {
        super(name);
    }

    protected FakeResponse execDavRequest(String method, String url)
            throws ServletException, IOException {
        return execDavRequest(method, url, null);
    }

    protected FakeResponse execDavRequest(String method, String url, String data)
            throws ServletException, IOException {
        FakeRequest fReq = new FakeRequest(method, url);
        if (data != null) {
            FakeServletInputStream in = new FakeServletInputStream(data);
            fReq.setStream(in);
        }
        return execDavRequest(fReq);
    }

    /*
     * protected FakeResponse execDavRequest(String method, String url,
     * InputStream in) throws ServletException, IOException { FakeRequest fReq =
     * new FakeRequest(method,url); if (in!=null) { FakeServletInputStream fin =
     * new FakeServletInputStream(in); fReq.setStream(fin); } return
     * execDavRequest(fReq); }
     */

    protected FakeResponse execDavRequest(FakeRequest fReq)
            throws ServletException, IOException {

        FakeResponse fRes = new FakeResponse();
        // WebDavRequestWrapper dReq = new WebDavRequestWrapper(fReq);
        // WebDavResponseWrapper dRes = new WebDavResponseWrapper(fRes);
        fReq.setAttribute("CoreSession", remote);
        NuxeoWebDavServlet davServlet = new NuxeoWebDavServlet();

        davServlet.service(fReq, fRes);

        return fRes;
    }

}
