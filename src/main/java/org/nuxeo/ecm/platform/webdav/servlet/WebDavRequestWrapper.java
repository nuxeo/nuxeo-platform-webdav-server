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
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.platform.locking.adapters.DavLockInfo;
import org.nuxeo.ecm.platform.webdav.config.WebDavClientConfigurationDescriptor;
import org.nuxeo.ecm.platform.webdav.config.WebDavConfigurationService;
import org.nuxeo.ecm.platform.webdav.helpers.DavRequestXMLHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * Request wrapper for WebDAV requests.
 * <p>
 * Encapsulates WebDAV-specific methods.
 *
 * @author tiry
 */
public class WebDavRequestWrapper extends HttpServletRequestWrapper {

    private static final String INCLUDE_REQUEST_URI_ATTR = "javax.servlet.include.request_uri";

    private static final String INCLUDE_PATH_INFO_ATTR = "javax.servlet.include.path_info";

    private static final String INCLUDE_SERVLET_PATH_ATTR = "javax.servlet.include.servlet_path";

    private static final String CLIENT_CONFIG_SESSION_KEY = "org.nuxeo.ecm.platform.webdav.clientConfig";

    private static final Log log = LogFactory.getLog(WebDavRequestWrapper.class);

    private final HttpServletRequest req;

    private String headerDepth;

    private String headerDestination;

    private String headerLockTocken;

    private boolean headerOverwrite;

    private String headerTimeout;

    private Element davParameters;

    private WebDavClientConfigurationDescriptor clientConfig;

    public WebDavRequestWrapper(HttpServletRequest request) {
        super(request);
        req = request;
        parseHeaders();
        parseBody();
    }

    public String getRelativePath() {
        // Are we being processed by a RequestDispatcher.include()?
        if (req.getAttribute(INCLUDE_REQUEST_URI_ATTR) != null) {
            String result = (String) req.getAttribute(INCLUDE_PATH_INFO_ATTR);
            if (result == null) {
                result = (String) req.getAttribute(INCLUDE_SERVLET_PATH_ATTR);
            }
            if (result == null || result.equals("")) {
                result = "/";
            }
            return result;
        }

        // Now, extract the desired path directly from the request
        String result = req.getPathInfo();
        if (result == null) {
            result = req.getServletPath();
        }
        if (result == null || result.equals("")) {
            result = "/";
        }

        // check for dummy URLs
        if (result.contains('/' + MSDavConst.MS_DAV_URL_DELIMITER + '/')) {
            String[] urlParts = result.split(MSDavConst.MS_DAV_URL_DELIMITER);
            result = urlParts[0];
        }

        return result;
    }

    public Map<String, String> getRequestedProperties() {
        Map<String, String> requestedProps = new HashMap<String, String>();

        return requestedProps;
    }

    protected void parseHeaders() {
        // Depth
        String h = req.getHeader(WebDavConst.HEADER_DEPTH);
        if (h != null) {
            String headerValue = h.trim().toLowerCase();
            if (headerValue.equals("0") || headerValue.equals("1")
                    || headerValue.equals(WebDavConst.DAV_DEPTH_INFINITY)) {
                headerDepth = headerValue;
            } else {
                headerDepth = WebDavConst.DAV_DEPTH_INFINITY;
            }
        } else {
            headerDepth = WebDavConst.DAV_DEPTH_INFINITY;
        }

        // Destination
        h = req.getHeader(WebDavConst.HEADER_DESTINATION);
        if (h != null) {
            headerDestination = h;
        }

        // Lock-Tocken
        h = req.getHeader(WebDavConst.HEADER_LOCKTOCKEN);
        if (h != null) {
            headerLockTocken = h;
        }

        // Overwrite
        h = req.getHeader(WebDavConst.HEADER_OVERWRITE);
        if (h != null) {
            headerOverwrite = h.equalsIgnoreCase("T");
        }

        // Timeout
        h = req.getHeader(WebDavConst.HEADER_TIMEOUT);
        if (h != null) {
            headerTimeout = h;
        }

        setupForUserAgent();
    }

    protected void setupForUserAgent() {
        HttpSession hsession = req.getSession(false);
        if (hsession != null) {
            clientConfig = (WebDavClientConfigurationDescriptor) hsession.getAttribute(CLIENT_CONFIG_SESSION_KEY);
        }

        if (clientConfig == null) {
            String ua = req.getHeader("User-Agent");
            WebDavConfigurationService configService = (WebDavConfigurationService) Framework.getRuntime().getComponent(
                    WebDavConfigurationService.NAME);
            if (configService == null) {
                log.error("Unable to connect to WebDav configuration service");
            } else {
                clientConfig = configService.getClientConfig(ua);

                if (hsession != null) {
                    hsession.setAttribute(CLIENT_CONFIG_SESSION_KEY,
                            clientConfig);
                }
            }
        }

        if (clientConfig == null) {
            log.debug("Unable to configure Dav Client");
        } else {
            log.debug("Client configured using : " + clientConfig.getName());
        }
    }

    protected void parseBody() {
        String cl = req.getHeader("Content-Length");
        if (cl != null && cl.equals("0")) {
            return;
        }
        if (!req.getMethod().equals(WebDavConst.METHOD_PROPFIND)) {
            return;
        }

        try {
            InputStream bodyStream = req.getInputStream();
            if (bodyStream != null) {
                davParameters = getDocumentRoot(bodyStream);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.error("Error while parsing request Body : " + e.getMessage());
        }
    }

    private static Element getDocumentRoot(InputStream stream) {
        try {
            SAXReader saxReader = new SAXReader();
            //saxReader.setEntityResolver(new DTDEntityResolver());
            saxReader.setMergeAdjacentText(true);
            return saxReader.read(stream).getRootElement();
        } catch (DocumentException de) {
            log.error("Error while parsing XML request Body : " + de.getMessage());
            try {
                int size = stream.available();
                byte[] buffer = new byte[size];
                stream.read(buffer);
                log.error("input stream = " + buffer);
            } catch (IOException e) {
                log.error("Unable to read input buffer : " + e.getMessage());
            }

            return null;
            //throw new RuntimeException(de);
        }
    }

    // Simple getters

    public String getHeaderDepth() {
        if (headerDepth == null) {
            return WebDavConst.DAV_DEPTH_INFINITY;
        } else {
            return headerDepth;
        }
    }

    public String getHeaderDestination(boolean preprocess) {
        if (!preprocess) {
            return headerDestination;
        } else {
            // handle preprocesing of this header
            String processedDestination = headerDestination;

            if (processedDestination.contains("?"
                    + NuxeoWebDavServlet.GET_PARAMETER_DECORATOR)) {
                String[] destParts = processedDestination.split("\\?"
                        + NuxeoWebDavServlet.GET_PARAMETER_DECORATOR + "=");
                String destURIPart = destParts[0];
                Path destPath = new Path(destURIPart);
                destPath = destPath.removeLastSegments(1);
                String destVPart = destParts[1];
                String[] destVSubParts = destVPart.split("/");
                processedDestination = destPath.toString() + "/"
                        + destVSubParts[destVSubParts.length - 1];
            }

            // add URL escaping with default encoding (system encoding)
            processedDestination = URLDecoder.decode(processedDestination);

            return processedDestination;
        }
    }

    public String getHeaderDestination() {
        return getHeaderDestination(true);
    }

    public String getHeaderLockTocken() {
        return headerLockTocken;
    }

    public boolean getHeaderOverwrite() {
        return headerOverwrite;
    }

    public String getHeaderTimeout() {
        return headerTimeout;
    }

    public Element getDavParameters() {
        return davParameters;
    }

    public Map<String, List<String>> extractRequestedProperties() {
        return DavRequestXMLHelper.extractProperties(davParameters);
    }

    public DavLockInfo extractLockInfo() {
        return DavRequestXMLHelper.extractLockInfo(davParameters);
    }

    public boolean needVirtualPathForLief() {
        // for tests
        if (req.getHeader("needVirtualPathForLief") != null) {
            return true;
        }
        if (clientConfig != null) {
            return clientConfig.getNeedVirtualPathForLief();
        }
        return false;
    }

    public boolean needGetParameterForCollectionNamming() {
        // for tests
        if (req.getHeader("needGetParameterForCollectionNamming") != null) {
            return true;
        }
        if (clientConfig != null) {
            return clientConfig.getNeedGetParameterForCollectionNamming();
        }
        return false;
    }

    public boolean needFullURLs() {
        // for tests
        if (req.getHeader("needFullURLs") != null) {
            return true;
        }
        if (clientConfig != null) {
            return clientConfig.getNeedFullURLs();
        }
        return false;
    }

    public boolean skipLevel0ForListing() {
        // for tests
        if (req.getHeader("skipLevel0ForListing") != null) {
            return true;
        }
        if (clientConfig != null) {
            return clientConfig.getSkipLevel0ForListing();
        }
        return false;
    }

    public boolean isMSClient() {
        if (clientConfig != null) {
            return clientConfig.getNeedMSDavHeader();
        }
        return false;
    }

    public boolean useFileNameAsRessourceName() {
        // for tests
        if (req.getHeader("useFileNameAsRessourceName") != null) {
            return true;
        }
        if (clientConfig != null) {
            return clientConfig.getUseFileNameAsRessourceName();
        }
        return false;
    }

}
