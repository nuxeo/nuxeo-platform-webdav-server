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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.nuxeo.ecm.platform.webdav.helpers.DavResponseXMLHelper;

/**
 * Response wrapper for WebDAV requests.
 * <p>
 * Encapsulates WebDAV-specific methods.
 *
 * @author tiry
 */
public class WebDavResponseWrapper extends HttpServletResponseWrapper {

    private static final Log log = LogFactory.getLog(WebDavResponseWrapper.class);

    private final HttpServletResponse response;

    // resourceURI => SchemaURL => Map(propName, propValue)
    private Map<String, Map<String, Map<String, String>>> davProperties;

    private final DavResponseXMLHelper xmlHelper;

    public WebDavResponseWrapper(HttpServletResponse response) {
        super(response);
        this.response = response;
        xmlHelper = new DavResponseXMLHelper();

    }

    public void setUnimplemented() {
        response.setStatus(SC_NOT_IMPLEMENTED);
    }

    /**
     * Stores properties in the response buffer. This method does not write the
     * XML output Use writeProperties for that.
     *
     * @param resourceURL
     * @param schemaURI
     * @param props
     */
    public void addProperties(String resourceURL, String schemaURI,
            Map<String, String> props) {
        if (davProperties == null) {
            davProperties = new HashMap<String, Map<String, Map<String, String>>>();
        }

        Map<String, Map<String, String>> resourceProperties = davProperties.get(resourceURL);

        if (resourceProperties == null) {
            resourceProperties = new HashMap<String, Map<String, String>>();
        }

        if (resourceProperties.get(schemaURI) != null) {
            resourceProperties.get(schemaURI).putAll(props);
        } else {
            resourceProperties.put(schemaURI, props);
        }

        davProperties.put(resourceURL, resourceProperties);
    }

    public void writeProperties() {
        if (davProperties == null) {
            return;
        }

        for (String resourceURL : davProperties.keySet()) {
            Map<String, Element> propsTag = xmlHelper.addResourceToResponse(resourceURL);

            for (String schemaURI : davProperties.get(resourceURL).keySet()) {
                xmlHelper.addResourcePropertiesToResponse(schemaURI,
                        davProperties.get(resourceURL).get(schemaURI),
                        propsTag);
            }
        }

        try {
            String xmlResult = xmlHelper.getAsXMLString();
            getWriter().write(xmlResult);
            log.debug("NuxeoDavResponse XML Body= \n" + xmlResult);
        } catch (IOException e) {
            log.error(e);
        }
    }

}
