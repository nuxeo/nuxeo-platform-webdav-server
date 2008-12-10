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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.webdav.mapping.ApacheDavPropertiesMapper;
import org.nuxeo.ecm.platform.webdav.mapping.DavPropertiesMapper;
import org.nuxeo.ecm.platform.webdav.mapping.NullPropertiesMapper;
import org.nuxeo.ecm.platform.webdav.mapping.PropertiesMapper;
import org.nuxeo.ecm.platform.webdav.mapping.SchemaPropertiesMapper;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;

/**
 * Factory for properties mappers.
 *
 * @author tiry
 */
public class MappingHelper {

    public static final String PROP_AS_TAG_PREFIX = "TAG:";

    private static final String APACHE_DAV_NS = "http://apache.org/dav/props/";

    // Utility class.
    private MappingHelper() {
    }

    public static PropertiesMapper getMapperForURI(String uri) {
        if (uri.equals(WebDavConst.DAV_XML_URI)) {
            return new DavPropertiesMapper();
        } else if (uri.startsWith(WebDavConst.NUXEO_SCHEMA_NS)) {
            return new SchemaPropertiesMapper(uri);
        } else if (uri.equals(APACHE_DAV_NS)) {
            return new ApacheDavPropertiesMapper();
        } else {
            return new NullPropertiesMapper();
        }
    }

    public static Map<String, Map<String, String>> getAllProperties(
            DocumentModel doc) throws ClientException {

        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

        // Dav Properties
        Map<String, String> davProp = getMapperForURI(WebDavConst.DAV_XML_URI).getAllDavProperties(
                doc);
        result.put(WebDavConst.DAV_XML_URI, davProp);

        // Nuxeo Properties
        /*for (String schema : doc.getDeclaredSchemas()) {
            String schemaURI = WebDavConst.NUXEO_SCHEMA_NS + schema;
            result.put(schemaURI,
                    getMapperForURI(schemaURI).getAllDavProperties(doc));
        }*/

        return result;
    }

    public static Map<String, List<String>> getAllPropertyNames(
            DocumentModel doc) throws ClientException {
        Map<String, List<String>> result = new HashMap<String, List<String>>();

        // Dav Properties
        List<String> davPropNames = getMapperForURI(WebDavConst.DAV_XML_URI).getDavPropertiesNames(
                doc);
        result.put(WebDavConst.DAV_XML_URI, davPropNames);

        // Nuxeo Properties
        for (String schema : doc.getDeclaredSchemas()) {
            String schemaURI = WebDavConst.NUXEO_SCHEMA_NS + schema;
            result.put(schemaURI,
                    getMapperForURI(schemaURI).getDavPropertiesNames(doc));
        }

        return result;
    }

}
