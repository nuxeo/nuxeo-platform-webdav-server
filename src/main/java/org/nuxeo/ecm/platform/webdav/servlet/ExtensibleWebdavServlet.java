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
 * $Id: JOOoConvertPluginImpl.java 18651 2007-05-13 20:28:53Z sfermigier $
 */

package org.nuxeo.ecm.platform.webdav.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for the Nuxeo WebDAV Servlet.
 *
 * @author tiry
 */
public class ExtensibleWebdavServlet extends HttpServlet {

    private static final long serialVersionUID = 965764764858L;

    private static final Log log = LogFactory.getLog(ExtensibleWebdavServlet.class);

    /**
     * Handles the WebDAV methods.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        String method = req.getMethod();

        // force session creation, because some clients are too dummy
        req.getSession(true);

        WebDavRequestWrapper davRequest = new WebDavRequestWrapper(req);
        WebDavResponseWrapper davResponse = new WebDavResponseWrapper(resp);
        String path = davRequest.getRelativePath();
        log.debug('[' + method + "] " + path);

        if (method.equals(WebDavConst.METHOD_PROPFIND)) {
            doPropfind(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_PROPPATCH)) {
            doProppatch(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_MKCOL)) {
            doMkcol(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_COPY)) {
            doCopy(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_MOVE)) {
            doMove(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_LOCK)) {
            doLock(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_UNLOCK)) {
            doUnlock(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_POST)) {
            doPost(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_PUT)) {
            doPut(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_GET)) {
            doGet(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_DELETE)) {
            doDelete(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_OPTIONS)) {
            doOptions(davRequest, davResponse);
        } else if (method.equals(WebDavConst.METHOD_HEAD)) {
            doHead(davRequest, davResponse);
        } else {
            throw new ServletException("Unsupported method");
        }

        //davResponse.addHeader("MS-Author-Via", "DAV");
        //davResponse.setCharacterEncoding("utf-8");
    }

    protected void doPropfind(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doMove(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doLock(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doUnlock(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doMkcol(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doCopy(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doProppatch(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doPut(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doPost(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doGet(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doHead(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doDelete(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

    protected void doOptions(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        davResponse.setUnimplemented();
    }

}
