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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.ecm.platform.locking.adapters.DavLockInfo;
import org.nuxeo.ecm.platform.locking.adapters.LockInfo;
import org.nuxeo.ecm.platform.locking.adapters.LockableDocument;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.ecm.platform.webdav.adapters.DavResourceAdapter;
import org.nuxeo.ecm.platform.webdav.helpers.CoreHelper;
import org.nuxeo.ecm.platform.webdav.helpers.MappingHelper;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;
import org.nuxeo.runtime.api.Framework;

/**
 * Nuxeo WebDAV Servlet.
 * 
 * @author tiry
 */
public class NuxeoWebDavServlet extends ExtensibleWebdavServlet {

    public static final String PATTERN = "/nuxeo/dav/";

    public static final String GET_PARAMETER_DECORATOR = "displayName";

    private static final long serialVersionUID = 876875851L;

    private static final Log log = LogFactory.getLog(NuxeoWebDavServlet.class);

    @Override
    protected void doPropfind(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doPropfind");

        DocumentModel doc;
        try {
            doc = CoreHelper.resolveTarget(davRequest);
            if (doc == null)
                throw new ClientException("File not found.");
        } catch (ClientException e) {
            log.error("Error while getting document : " + e.getMessage());
            davResponse.setStatus(WebDavConst.SC_NOT_FOUND);
            return;
        } catch (Exception e) {
            log.error("Error while getting document : " + e.getMessage());
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // read requested properties from request
        Map<String, List<String>> requestedProperties = davRequest.extractRequestedProperties();

        String depth = davRequest.getHeaderDepth();
        davResponse.setCharacterEncoding("UTF-8");
        davResponse.setContentType("text/xml");

        // recursive calls to extract the properties from the target doc
        try {
            writeProperties(davResponse, requestedProperties, doc, depth, 0,
                    davRequest);
        } catch (ClientException e) {
            log.error("Error while writing properties to document : "
                    + e.getMessage());
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        davResponse.writeProperties();
        davResponse.setLocale(Locale.US);
        davResponse.setStatus(WebDavConst.SC_MULTI_STATUS, "Multi-Status");
        // davResponse.setStatus(WebDavConst.SC_MULTI_STATUS);
    }

    protected static void writeProperties(WebDavResponseWrapper davResponse,
            Map<String, List<String>> requestedProperties, DocumentModel doc,
            String maxDepth, int depth, WebDavRequestWrapper davRequest)
            throws ClientException {
        if (!maxDepth.equals(WebDavConst.DAV_DEPTH_INFINITY)) {
            if (maxDepth.equals("0")) {
                if (depth > 0) {
                    return;
                }
            } else if (maxDepth.equals("1")) {
                if (depth > 1) {
                    return;
                }
            }
        }

        // dump current doc properties
        for (String uri : requestedProperties.keySet()) {
            if (uri.equals("*")) {
                Map<String, Map<String, String>> allProps = MappingHelper.getAllProperties(doc);
                for (String schemaURI : allProps.keySet()) {
                    // davResponse.addProperties(doc.getPathAsString(),
                    // schemaURI, allProps.get(schemaURI));
                    davResponse.addProperties(getRessourceURL(doc, davRequest,
                            depth), schemaURI, allProps.get(schemaURI));
                }
            } else if (uri.equals("propname")) {
                Map<String, List<String>> allPropNames = MappingHelper.getAllPropertyNames(doc);
                Map<String, Map<String, String>> allEmptyProps = new HashMap<String, Map<String, String>>();
                for (String schemaURI : allPropNames.keySet()) {
                    Map<String, String> emptyProps = new HashMap<String, String>();
                    for (String propName : allPropNames.get(schemaURI)) {
                        emptyProps.put(propName, null);
                    }
                    /*
                     * if (davRequest.skipLevel0ForListing()) { boolean
                     * canSkip=false; // check if we can skip Level0 Listing if
                     * (depth==0 && !maxDepth.equals("0") && doc.isFolder()) {
                     * 
                     * } } else davResponse.addProperties(getRessourceURL(doc,
                     * davRequest, depth), schemaURI, emptyProps);
                     */

                    if (!(depth == 0 && davRequest.skipLevel0ForListing())) {
                        davResponse.addProperties(getRessourceURL(doc,
                                davRequest, depth), schemaURI, emptyProps);
                    }
                }

            } else {
                List<String> fieldNames = requestedProperties.get(uri);
                Map<String, String> props = MappingHelper.getMapperForURI(uri).getDavProperties(
                        doc, fieldNames);
                if (!(depth == 0 && davRequest.skipLevel0ForListing())) {
                    davResponse.addProperties(getRessourceURL(doc, davRequest,
                            depth), uri, props);
                }
            }
        }

        // see if there are chidren
        if (doc.isFolder()) {
            CoreSession session;
            try {
                session = CoreHelper.getAssociatedCoreSession(davRequest);
            } catch (Exception e) {
                log.error("Unable to get asociated session:" + e.getMessage());
                return;
            }
            try {
                for (DocumentModel child : session.getChildren(doc.getRef())) {
                    writeProperties(davResponse, requestedProperties, child,
                            maxDepth, depth + 1, davRequest);
                }
            } catch (ClientException e) {
                log.error("Error while writing properties in XML Body : "
                        + e.getMessage(), e);
                return;
            } catch (Throwable t) {
                log.error("Error while writing properties in XML Body : "
                        + t.getMessage(), t);
                return;
            }
        }
    }

    @Override
    protected void doMove(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doMove");
        doCopyOrMove(davRequest, davResponse, false);
    }

    @Override
    protected void doLock(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doLock");
        try {
            DavLockInfo lockInfo = davRequest.extractLockInfo();

            DocumentModel target = null;
            try {
                target = CoreHelper.resolveTarget(davRequest);
            } catch (Exception ex) {
                // do nothing
            }

            if (target == null) {

                int idx = CoreHelper.getDocumentPath(davRequest).lastIndexOf(
                        '/');
                final String fileName = CoreHelper.getDocumentPath(davRequest).substring(
                        idx + 1);
                final String filePath = CoreHelper.getDocumentPath(davRequest).substring(
                        0, idx);

                CoreSession session = CoreHelper.getAssociatedCoreSession(davRequest);
                target = session.createDocumentModel("File");

                File tmpfile = File.createTempFile("NuxeoWebDavServlet", "tmp");
                InputStream input = new ByteArrayInputStream(new byte[] { 'T',
                        'E', 'S', 'T' });
                FileUtils.copyToFile(input, tmpfile);

                FileBlob thefile = new FileBlob(tmpfile,
                        "application/octet-stream", null);
                target.setProperty("file", "content", thefile);
                target.setProperty("file", "filename", fileName);
                target.setPropertyValue("dc:title", fileName);
                target.setPathInfo(filePath, fileName);

                target = session.createDocument(target);
                session.save();
            }

            LockableDocument lockableDoc = target.getAdapter(LockableDocument.class);
            int result = lockableDoc.lock("Administrator");

            if (result >= 0) {
                LockInfo nxLockInfo = lockableDoc.getLockInfo();
                String lockToken = nxLockInfo.getToken();
                davResponse.setStatus(WebDavConst.SC_OK);

                final String response = "<D:multistatus xmlns:D=\"DAV:\">"
                        + "<D:lockdiscovery>" + "<D:activelock>"
                        + "<D:locktype>" + "<D:write/></D:locktype>"
                        + "<D:lockscope>" + "<D:exclusive/></D:lockscope>"
                        + "<D:depth>0</D:depth>" + "<D:owner>admin</D:owner>"
                        + "<D:timeout>Second-179</D:timeout>" + "<D:locktoken>"
                        + "<D:href>opaquelocktoken:" + target.getId()
                        + ":Administrator</D:href>" + "</D:locktoken>"
                        + "</D:activelock>" + "</D:lockdiscovery>"
                        + "</D:multistatus>";
                davResponse.setStatus(WebDavConst.SC_OK);
                OutputStream os = davResponse.getOutputStream();
                os.write(response.getBytes());
                os.close();
                return;

            } else {
                davResponse.setStatus(WebDavConst.SC_LOCKED);
                return;
            }

        } catch (ClientException e) {
            log.error(e);
            davResponse.setStatus(WebDavConst.SC_NOT_FOUND);
            return;
        } catch (Exception e) {
            log.error(e);
            davResponse.setStatus(WebDavConst.SC_NOT_FOUND);
            return;
        }
    }

    @Override
    protected void doUnlock(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doUnlock");
        try {
            DavLockInfo lockInfo = davRequest.extractLockInfo();

            DocumentModel target = CoreHelper.resolveTarget(davRequest);
            LockableDocument lockableDoc = target.getAdapter(LockableDocument.class);
            int result = lockableDoc.unlock("Administrator");

            if (result >= 0) {
                davResponse.setStatus(WebDavConst.SC_NO_CONTENT);
                return;
            } else {
                davResponse.setStatus(WebDavConst.SC_LOCKED);
                return;
            }

        } catch (ClientException e) {
            log.error(e);
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (Exception e) {
            log.error(e);
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }

    @Override
    protected void doMkcol(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doMkcol");

        String path = CoreHelper.getDocumentPath(davRequest);
        String[] pathParts = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (part.length() > 0) {
                sb.append('/').append(part);
            }
        }
        String colName = pathParts[pathParts.length - 1];
        String containerPath = sb.append('/').toString();

        log.debug("Creating Collection " + colName + " at path "
                + containerPath);

        // resolve parent
        // DocumentRef docRef = new PathRef(containerPath);
        DocumentModel parent;
        try {
            parent = CoreHelper.resolveTarget(davRequest, containerPath);
        } catch (ClientException e) {
            log.error(
                    "Error while getting parent document : " + e.getMessage(),
                    e);
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (Exception e) {
            log.error(
                    "Error while getting parent document : " + e.getMessage(),
                    e);
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (parent == null) {
            log.error("Unable to find Parent Folder");
            davResponse.setStatus(WebDavConst.SC_CONFLICT);
            return;
        }

        if (!parent.isFolder()) {
            log.error("Can not create a collection as child of a non folderish item");
            davResponse.setStatus(WebDavConst.SC_FORBIDDEN);
            return;
        }

        // get the session
        CoreSession session;
        try {
            session = CoreHelper.getAssociatedCoreSession(davRequest);
        } catch (Exception e) {
            log.error("Error while getting session : " + e.getMessage(), e);
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // check that children does not already exists
        try {
            if (session.hasChildren(new PathRef(path))) {
                log.error("Error collection already exists");
                davResponse.setStatus(WebDavConst.SC_METHOD_NOT_ALLOWED);
                return;
            }
        } catch (ClientException e) {
            log.error("Error while calling hasChildren : " + e.getMessage(), e);
        }

        // determine type of the collection
        String parentType = parent.getType();
        String targetType = "Folder";

        // XXX dummy impl
        if (parentType.equals("WorkspaceRoot")) {
            targetType = "Workspace";
        } else if (parentType.equals("SectionRoot")) {
            targetType = "Section";
        } else if (parentType.equals("Root")) {
            targetType = "Domain";
        }

        TypeInfo typeAdapter = parent.getAdapter(TypeInfo.class);
        if (typeAdapter != null) {
            List<String> allowedTypes = new ArrayList<String>(
                    Arrays.asList(typeAdapter.getAllowedSubTypes()));

            if (!allowedTypes.contains(targetType)) {
                log.error("Can not create type " + targetType
                        + " as child of a " + parentType);
                davResponse.setStatus(WebDavConst.SC_FORBIDDEN);
                return;
            }
        }

        // create the new collection
        try {
            String colId = IdUtils.generateId(colName);
            DocumentModel collec = session.createDocumentModel(
                    parent.getPathAsString(), colId, targetType);
            collec.setProperty("dublincore", "title", colName);
            collec = session.createDocument(collec);

            // because most client assume name==displayname
            // after creating a folder they will try to rename
            // it using the label as path
            // => store in cache to ebable resolution
            String virtualURL = '/' + collec.getRepositoryName()
                    + parent.getPathAsString() + '/' + colName;
            String virtualURL2;
            if (containerPath.endsWith("/")) {
                virtualURL2 = '/' + collec.getRepositoryName() + containerPath
                        + colName;
            } else {
                virtualURL2 = '/' + collec.getRepositoryName() + containerPath
                        + '/' + colName;
            }

            URLResolverCache.addToCache(virtualURL, collec);
            URLResolverCache.addToCache(virtualURL2, collec);

            session.save();
        } catch (ClientException e) {
            log.error("Error while getting session : " + e.getMessage(), e);
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        davResponse.setStatus(WebDavConst.SC_CREATED);
    }

    @Override
    protected void doCopy(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doCopy");
        doCopyOrMove(davRequest, davResponse, true);
    }

    protected void doCopyOrMove(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse, boolean copy) {
        log.debug("doCopy");
        try {
            DocumentModel source = CoreHelper.resolveTarget(davRequest);
            CoreSession session = CoreHelper.getAssociatedCoreSession(davRequest);

            String destination = davRequest.getHeaderDestination();
            if (destination == null) {
                davResponse.setStatus(WebDavConst.SC_BAD_REQUEST);
                log.error("Copy with null destination");
                return;
            }

            if (destination.equals(source.getPathAsString())) {
                davResponse.setStatus(WebDavConst.SC_FORBIDDEN);
                log.error("Source and destination are the same");
                return;
            }

            // try to resolve the destination path
            List<String> targetInfo = getPathFromFullURL(davRequest,
                    destination);

            if (!targetInfo.get(0).equals(source.getRepositoryName())) {
                davResponse.setStatus(WebDavConst.SC_FORBIDDEN);
                log.error("Can not copy between 2 repositories");
                return;
            }

            String targetPath = targetInfo.get(1);

            // check overwrite condition

            DocumentModel targetDoc = CoreHelper.resolveTarget(davRequest,
                    targetPath);
            boolean targetAlreadyExists = false;
            String targetDocumentName = null;
            if (targetDoc != null) {
                targetAlreadyExists = true;
                targetDocumentName = targetDoc.getName();
            }

            if (targetAlreadyExists) {
                if (!davRequest.getHeaderOverwrite()) {
                    // can't overwrite
                    davResponse.setStatus(WebDavConst.SC_PRECONDITION_FAILED);
                    log.error("target document exists and overwrite is set to False");
                    return;
                } else {
                    // check lock status !!
                    // XXXX

                    // prepare overwrite : delete the existing target
                    session.removeDocument(targetDoc.getRef());
                    session.save();
                }
            }

            // get the container path
            DocumentRef targetContainerRef;
            if (targetAlreadyExists) {
                targetContainerRef = targetDoc.getParentRef();
            } else {
                String[] pathParts = targetPath.split("/");

                targetDocumentName = pathParts[pathParts.length - 1];

                if (targetDocumentName.equals("")) {
                    targetDocumentName = pathParts[pathParts.length - 2];
                }

                String targetContainerPath = targetPath.replace(
                        '/' + targetDocumentName, "");

                DocumentModel targetContainer = CoreHelper.resolveTarget(
                        davRequest, targetContainerPath);

                if (targetContainer != null) {
                    targetContainerRef = targetContainer.getRef();
                } else {
                    log.error("destination container does not exist:"
                            + targetContainerPath);
                    davResponse.setStatus(WebDavConst.SC_CONFLICT);
                    return;
                }

                // XXX handle crappy clients
                if (targetDocumentName.contains("?" + GET_PARAMETER_DECORATOR)) {
                    targetDocumentName = targetDocumentName.split("\\?"
                            + GET_PARAMETER_DECORATOR + "=")[1];
                    if (targetDocumentName.contains("/")) {
                        String[] subCrap = targetDocumentName.split("/");
                        targetDocumentName = subCrap[subCrap.length - 1];
                    }
                }
                /*
                 * targetContainerRef = new PathRef(targetContainerPath); //
                 * check if target container does exists try {
                 * session.getDocument(targetContainerRef); } catch
                 * (ClientException e) { log.error("destination container does
                 * not exist"); davResponse.setStatus(WebDavConst.SC_CONFLICT);
                 * return; }
                 */
            }

            if (copy) {
                session.copy(source.getRef(), targetContainerRef,
                        targetDocumentName);
                session.save();
            } else {
                DocumentModel sourceContainer = session.getDocument(source.getParentRef());
                if (sourceContainer.getRef().equals(targetContainerRef)) {
                    // simple rename
                    DavResourceAdapter adapter = source.getAdapter(DavResourceAdapter.class);
                    adapter.rename(targetDocumentName);
                    URLResolverCache.removeFromCache(source);

                    session.saveDocument(source);
                    session.save();
                    targetAlreadyExists = true;
                } else {
                    // real move
                    session.move(source.getRef(), targetContainerRef,
                            targetDocumentName);
                    session.save();
                    URLResolverCache.removeFromCache(source);
                }
            }

            if (targetAlreadyExists) {
                davResponse.setStatus(WebDavConst.SC_NO_CONTENT);
            } else {
                davResponse.setStatus(WebDavConst.SC_CREATED);
            }

        } catch (Exception e) {
            davResponse.setStatus(WebDavConst.SC_NOT_FOUND);
            log.error(e.getMessage(), e);
        }
    }

    @Override
    protected void doProppatch(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doProppatch");
        davResponse.setUnimplemented();
    }

    @Override
    protected void doPut(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {

        String path = CoreHelper.getDocumentPath(davRequest); // davRequest.
        // getRelativePath
        // ();
        String[] pathParts = path.split("/");
        String fileName = pathParts[pathParts.length - 1];
        String containerPath = (path + ' ').replace('/' + fileName + ' ', "/");
        File tmpfile = null;

        try {

            CoreSession session;
            try {
                session = CoreHelper.getAssociatedCoreSession(davRequest);
            } catch (Exception e1) {
                log.error("Unable to get Core Session : " + e1.getMessage());
                davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            FileManager fm;
            try {
                fm = Framework.getService(FileManager.class);
            } catch (Exception e) {
                log.error("Unable to get FileManager : " + e.getMessage());
                davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            InputStream input;
            try {
                input = davRequest.getInputStream();
            } catch (IOException e) {
                log.error("Unable to read request inputStream : "
                        + e.getMessage());
                davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            try {
                tmpfile = File.createTempFile("NuxeoWebDavServlet", "tmp");
                FileUtils.copyToFile(input, tmpfile);
            } catch (IOException e) {
                log.error("Error while copying blob to tmp file :"
                        + e.getMessage());
            }

            StreamingBlob thefile = StreamingBlob.createFromFile(tmpfile);
            thefile.setMimeType(davRequest.getContentType());

            DocumentModel existingDoc = null;
            boolean alreadyExist = false;
            try {
                existingDoc = CoreHelper.resolveTarget(davRequest, path);
                if (existingDoc != null) {
                    alreadyExist = true;
                }
            } catch (ClientException e) {
                log.error(e);
            } catch (Exception e) {
                log.error(e);
            }

            try {
                // handle crappy paths heres
                if (davRequest.needGetParameterForCollectionNamming()) {

                    DocumentModel container = CoreHelper.resolveTarget(
                            davRequest, containerPath);
                    if (container == null) {
                        davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }
                    containerPath = container.getPathAsString();
                }

                if (alreadyExist) {
                    // URLResolverCache.removeFromCache(existingDoc);
                    // session.removeDocument(existingDoc.getRef());

                    existingDoc.setPropertyValue("file:content", thefile);
                    session.saveDocument(existingDoc);
                    session.save();
                }

                boolean overwrite = davRequest.getHeaderOverwrite();
                fm.createDocumentFromBlob(session, thefile, containerPath,
                        overwrite, fileName);
                session.save();
            } catch (Exception e) {
                log.error("Error when creating the document via FileManager: "
                        + e.getMessage(), e);
                davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            if (alreadyExist) {
                davResponse.setStatus(WebDavConst.SC_NO_CONTENT);
            } else {
                davResponse.setStatus(WebDavConst.SC_CREATED, "Created");
                davResponse.setContentType("text/html");
                try {
                    PrintWriter writer = davResponse.getWriter();
                    writer.write("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">.<HTML><HEAD><TITLE>201 Created</TITLE></HEAD>");
                    writer.write("<BODY> <H1>Created</H1> Resource ");
                    writer.write(davRequest.getPathInfo());
                    writer.write("has been created.</BODY></HTML>");
                    writer.flush();
                } catch (IOException e) {
                    log.error("Unable to write HTML output: " + e.getMessage(),
                            e);
                }
            }
        } finally {
            if (tmpfile != null) {
                tmpfile.delete();
                tmpfile = null;
            }
        }
    }

    @Override
    protected void doPost(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doPost");
        davResponse.setUnimplemented();
    }

    @Override
    protected void doGet(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doGet");
        doGetOrHead(davRequest, davResponse, true);
    }

    @Override
    protected void doHead(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doHead");
        doGetOrHead(davRequest, davResponse, false);
    }

    protected void doGetOrHead(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse, boolean includeBody) {
        log.debug("doGetOrHead");

        if (!includeBody) {
            NoBodyResponse response = new NoBodyResponse(
                    (HttpServletResponse) davResponse.getResponse());
            davResponse = new WebDavResponseWrapper(response);
        }

        try {
            DocumentModel target = CoreHelper.resolveTarget(davRequest);

            if (target == null) {
                davResponse.setStatus(WebDavConst.SC_NOT_FOUND);
                return;
            }

            DavResourceAdapter adapter = target.getAdapter(DavResourceAdapter.class);
            adapter.doGet(davRequest, davResponse);

            // if (davResponse.isCommitted()) {
            // return;
            // Include modification Date
            // XXX
            // }

            davResponse.setStatus(WebDavConst.SC_OK);

        } catch (ClientException e) {
            davResponse.setStatus(WebDavConst.SC_NOT_FOUND);
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            log.error(e.getMessage(), e);
        }
    }

    @Override
    protected void doDelete(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doDelete");
        try {
            DocumentModel target = CoreHelper.resolveTarget(davRequest);
            CoreSession session = CoreHelper.getAssociatedCoreSession(davRequest);
            session.removeDocument(target.getRef());
            session.save();
            URLResolverCache.removeFromCache(target);
        } catch (ClientException e) {
            davResponse.setStatus(WebDavConst.SC_NOT_FOUND);
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            davResponse.setStatus(WebDavConst.SC_INTERNAL_SERVER_ERROR);
            log.error(e.getMessage(), e);
        }

        davResponse.setStatus(WebDavConst.SC_NO_CONTENT);
    }

    @Override
    protected void doOptions(WebDavRequestWrapper davRequest,
            WebDavResponseWrapper davResponse) {
        log.debug("doOptions");
        // Dav Compatibility
        davResponse.addHeader(WebDavConst.HEADER_DAV, "1,2");

        // XXX : sould respond according to resource !
        davResponse.addHeader(
                "Allow",
                "GET, PUT, POST, HEAD, PROPFIND, PROPPATCH, OPTIONS, LOCK, UNLOCK, MOVE, COPY, MKCOL, DELETE");

        if (davRequest.isMSClient()) {
            // Add MS Header
            davResponse.addHeader("MS-Author-Via", "DAV");
        }

        davResponse.setStatus(WebDavConst.SC_OK);
    }

    private static String getRessourceURL(DocumentModel doc,
            WebDavRequestWrapper request, int depth) throws ClientException {
        if (depth > 0) {
            return virtualHostURL(request, getResourceURL(doc, request));
        } else {
            String requestedURL = request.getRequestURL().toString();
            if (request.needGetParameterForCollectionNamming()) {
                String qs = request.getQueryString();
                if (qs != null) {
                    requestedURL = requestedURL + '?' + qs;
                }
            }
            return virtualHostURL(request, requestedURL);
        }
    }

    private static String virtualHostURL(WebDavRequestWrapper request,
            String url) {
        if (url == null || !url.startsWith("http")) {
            return url;
        }

        String nuxeoVH = request.getHeader(WebDavConst.VH_HEADER);
        String forwardedHost = request.getHeader(WebDavConst.X_FORWARDED_HOST);

        if (nuxeoVH == null && forwardedHost == null) {
            return url;
        }

        int idx = url.indexOf(PATTERN) + PATTERN.length();
        String localBaseURL = url.substring(0, idx);

        String virtualHostedBaseURL;
        if (nuxeoVH != null) {
            if (nuxeoVH.endsWith("/")) {
                virtualHostedBaseURL = nuxeoVH.substring(0,
                        nuxeoVH.length() - 1)
                        + PATTERN;
            } else {
                virtualHostedBaseURL = nuxeoVH + PATTERN;
            }
        } else {
            virtualHostedBaseURL = request.getScheme() + "://" + forwardedHost
                    + PATTERN;
        }

        url = url.replace(localBaseURL, virtualHostedBaseURL);

        return url;
    }

    private static String getResourceURL(DocumentModel doc,
            WebDavRequestWrapper request) throws ClientException {

        String subPath = doc.getPathAsString();
        String baseURL = request.getRequestURL().toString();
        String resourceURL;
        if (baseURL.contains(subPath)) {
            resourceURL = baseURL;
        } else {
            // tmp hack
            int idx = baseURL.indexOf(PATTERN);
            idx += PATTERN.length();
            String startURL = baseURL.substring(0, idx);

            resourceURL = startURL + doc.getRepositoryName() + subPath;
        }

        if (!request.needFullURLs()) {
            int idx = resourceURL.indexOf(PATTERN);
            resourceURL = resourceURL.substring(idx);
        }

        if (doc.isFolder() && !resourceURL.endsWith("/")) {
            resourceURL += '/';
        }

        if (request.needVirtualPathForLief() && !doc.isFolder()) {
            // Append suffix for client that use last url part as resource label
            // ie : client that consider that name == displayname !
            DavResourceAdapter adapter = doc.getAdapter(DavResourceAdapter.class);
            String realFileName = adapter.getFileName();

            // some clients don't read the URL returned
            // they compute it via container URL + last part of child URL
            // because of this behavior, when using VirtualPaths, we need to
            // cache the truncated path in order to be able to resolve it
            String truncatedPath = resourceURL.replace(doc.getName(),
                    realFileName);
            URLResolverCache.addToCache(truncatedPath, doc);

            resourceURL = resourceURL + '/' + MSDavConst.MS_DAV_URL_DELIMITER
                    + '/' + realFileName;
        } else if (request.useFileNameAsRessourceName() && !doc.isFolder()) {
            // return display name as end of path
            DavResourceAdapter adapter = doc.getAdapter(DavResourceAdapter.class);
            String realFileName = adapter.getFileName();

            // cache the fake path in order to be able to resolve it
            String truncatedPath = resourceURL.replace(doc.getName(),
                    realFileName);
            URLResolverCache.addToCache(truncatedPath, doc);

            resourceURL = truncatedPath;
        }

        // Folders renaming according to DisplayName : Big Hack
        if (doc.isFolder() && request.needGetParameterForCollectionNamming()) {
            String truncatedPath = resourceURL.replace(doc.getName(),
                    doc.getTitle());
            URLResolverCache.addToCache(truncatedPath, doc);
            resourceURL = resourceURL + '?' + GET_PARAMETER_DECORATOR + "=/"
                    + doc.getTitle();
        }

        return resourceURL;
    }

    private static List<String> getPathFromFullURL(
            WebDavRequestWrapper request, String url) {
        List<String> result = new ArrayList<String>();

        String repoAndPath = url.split(PATTERN)[1];

        String repo = repoAndPath.split("/")[0];

        String path = repoAndPath.substring(repo.length());

        result.add(repo);
        result.add(path);

        return result;
    }

}
