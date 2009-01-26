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

package org.nuxeo.ecm.platform.webdav.helpers;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.webdav.adapters.DavResourceAdapter;
import org.nuxeo.ecm.platform.webdav.servlet.NuxeoWebDavServlet;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavRequestWrapper;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;
import org.nuxeo.runtime.api.Framework;

/**
 * Helper class to encapsulate Core access and path resolution
 *
 * @author tiry
 */

public class CoreHelper {

    // Utility class.
    private CoreHelper() {
    }

    public static String getRealPath(WebDavRequestWrapper req) {
        String rPath = (String) req.getAttribute("rPath");
        if (rPath == null) {
            String path = req.getRelativePath();
            // remove the /nuxeo/dav/ if present
            if (path.startsWith("/nuxeo/")) {
                path = path.substring(6);
            }
            if (path.startsWith(WebDavConst.DAV_URL_PREFIX)) {
                path = path.substring(WebDavConst.DAV_URL_PREFIX.length());
            }
            rPath = path;

            if (req.needGetParameterForCollectionNamming()) {
                // some crappy client will ignore the ? in the URL and append the path
                // to the QueryString :)
                String decorator = req
                        .getParameter(NuxeoWebDavServlet.GET_PARAMETER_DECORATOR);
                if (decorator != null) {
                    if (decorator.startsWith("/")) {
                        decorator = decorator.substring(1);
                    }
                    String[] subPath = decorator.split("/");
                    if (subPath.length > 1) {
                        for (int i = 1; i <= subPath.length - 1; i++) {
                            if (rPath.endsWith("/")) {
                                rPath += subPath[i];
                            } else {
                                rPath += '/' + subPath[i];
                            }
                        }
                    }
                }
            }

            req.setAttribute("rPath", rPath);
        }
        return rPath;
    }

    public static String getRepositoryName(WebDavRequestWrapper req) {
        String repoId = (String) req.getAttribute("repoId");
        if (repoId == null) {
            String path = getRealPath(req);
            repoId = path.split("/")[1];
            req.setAttribute("repoId", repoId);
        }
        return repoId;
    }

    public static String getDocumentPath(WebDavRequestWrapper req) {
        String path = getRealPath(req);
        String rep = getRepositoryName(req);
        return path.substring(rep.length() + 1);
    }

    public static CoreSession getAssociatedCoreSession(WebDavRequestWrapper req) throws Exception {
        CoreSession session = (CoreSession) req.getAttribute("CoreSession");
        if (session == null) {
            RepositoryManager rm = Framework.getService(RepositoryManager.class);
            Repository repo = rm.getRepository(getRepositoryName(req));
            if (repo == null) {
                throw new ClientException("Unable to get "
                        + getRepositoryName(req) + " repository");
            }
            session = repo.open();
            req.setAttribute("CoreSession", session);
        }
        return session;
    }


    public static DocumentModel resolveTarget(WebDavRequestWrapper req)
            throws Exception {
        String docPath = getDocumentPath(req);

        return resolveTarget(req, docPath);
    }

    public static DocumentModel resolveTarget(WebDavRequestWrapper req ,
            String docPath) throws Exception {

        String repo = getRepositoryName(req);
        DocumentRef docRef = URLResolverCache.resolveUrlFromCache(repo, docPath);
        if (docRef == null) {
            docRef = new PathRef(docPath);
        }

        try {
            return getAssociatedCoreSession(req).getDocument(docRef);
        } catch (ClientException e) {
            // XXX probly a path containing garbage
        }

        DocumentModel target = resolveVirtualPath(req, docPath, null);

        if (target != null) {
            String cacheableURL = target.getRepositoryName() + docPath;
            URLResolverCache.addToCache(cacheableURL, target);
        }
        return target;
    }

    /**
     * Resolves a virtual WebDAV path using the cache and repository browsing.
     *
     * @param req
     * @param containerPath
     * @param subPath
     * @return
     */
    public static DocumentModel resolveVirtualPath(WebDavRequestWrapper req,
            String containerPath, List<String> subPath) throws ClientException {
        CoreSession session;
        try {
            session = getAssociatedCoreSession(req);
        } catch (Exception e) {
            return null;
        }
        return resolveVirtualPath(session, containerPath, subPath);
    }

    /**
     * Resolve a virtual WebDav path using the cache and repository browsing.
     *
     * @param session
     * @param containerPath
     * @param subPath
     * @return
     */
    public static DocumentModel resolveVirtualPath(CoreSession session,
            String containerPath, List<String> subPath) throws ClientException {
        if (containerPath.endsWith("/")) {
            containerPath = containerPath.substring(0,
                    containerPath.length() - 1);
        }

        if (subPath == null) {
            subPath = new ArrayList<String>();
        }

        // separate parentPath from resource Name
        String[] pathParts = containerPath.split("/");
        if (!(pathParts.length > 1)) {
            return null;
        }
        String vpath = pathParts[pathParts.length - 1];
        String parentPath = (containerPath + ' ').replace('/' + vpath + ' ',
                "");

        if (parentPath.equals("")) {
            parentPath = "/";
        }

        if (vpath.equals("")) {
            return null;
        }

        // try to resolve parentPath
        //parentRef = URLResolverCache.resolveUrlFromCache(parentPath);
        DocumentRef parentRef = URLResolverCache.resolveUrlFromCache(
                session.getRepositoryName(), parentPath);
        if (parentRef == null) {
            // try directt JCR Path resolution
            parentRef = new PathRef(parentPath);
        }

        DocumentModel parent;
        try {
            boolean validPath = session.exists(parentRef);
            if (validPath) {
                parent = session.getDocument(parentRef);
                if (!parent.isFolder()) {
                    return null;
                }
                subPath.add(0, vpath);
            } else {
                // forward : try to resolve parent of parent ...
                subPath.add(0, vpath);
                return resolveVirtualPath(session, parentPath, subPath);
            }
        } catch (ClientException e) {
            // forward : try to resolve parent of parent ...
            subPath.add(0, vpath);
            return resolveVirtualPath(session, parentPath, subPath);
        }

        // parent should now be resolved
        DocumentModel targetDoc = parent;

        for (String vPath : subPath) {
            targetDoc = resolveVirtualChild(session, targetDoc, vPath);
            if (targetDoc == null) {
                return null;
            }
        }

        return targetDoc;
    }

    public static DocumentModel resolveVirtualChild(CoreSession session,
            DocumentModel container, String vPath) throws ClientException {
        if (!container.isFolder()) {
            return null;
        }
        DocumentModelList children;
        try {
            children = session.getChildren(container.getRef());
        } catch (ClientException e) {
            return null;
        }
        for (DocumentModel child : children) {
            DavResourceAdapter adapter = child.getAdapter(DavResourceAdapter.class);
            if (adapter == null) {
                return null;
            }
            if (vPath.equals(adapter.getFileName())) {
                URLResolverCache.addToCache(container, vPath, child);
                return child;
            } else if (vPath.equals(child.getName())) {
                URLResolverCache.addToCache(container, vPath, child);
                return child;
            }
        }

        return null;
    }

    @Deprecated
    public static DocumentModel resolveVirtualLiefPath(
            WebDavRequestWrapper req, String docPath) throws ClientException {
        CoreSession session;
        try {
            session = getAssociatedCoreSession(req);
        } catch (Exception e) {
            return null;
        }

        String[] pathParts = docPath.split("/");
        if (!(pathParts.length > 1)) {
            return null;
        }
        String vpath = pathParts[pathParts.length - 1];
        String parentPath = (docPath + ' ').replace('/' + vpath + ' ', "/");

        DocumentRef parentRef = URLResolverCache.resolveUrlFromCache(parentPath);
        if (parentRef == null) {
            parentRef = new PathRef(parentPath);
        }

        DocumentModelList children;
        try {
            DocumentModel parent = session.getDocument(parentRef);
            if (!parent.isFolder()) {
                return null;
            }
            children = session.getChildren(parentRef);
        } catch (ClientException e) {
            return null;
        }

        for (DocumentModel child : children) {
            DavResourceAdapter adapter = child
                    .getAdapter(DavResourceAdapter.class);
            if (adapter == null) {
                return null;
            }
            if (vpath.equals(adapter.getFileName())) {
                URLResolverCache.addToCache(docPath, child);
                return child;
            }
        }

        throw new ClientException("Unable to fetch document with path "
                + docPath);
    }

}
