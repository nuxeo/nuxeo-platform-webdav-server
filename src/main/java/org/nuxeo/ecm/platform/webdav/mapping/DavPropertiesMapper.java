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

package org.nuxeo.ecm.platform.webdav.mapping;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.webdav.adapters.DavResourceAdapter;
import org.nuxeo.ecm.platform.webdav.helpers.MappingHelper;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;

/**
 * Properties mapper for standard DAV properties.
 *
 * @author tiry
 */
public class DavPropertiesMapper implements PropertiesMapper {

    protected static final SimpleDateFormat davDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected static final SimpleDateFormat davDateFormat2 = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    static {
        davDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        davDateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public Map<String, String> getDavProperties(DocumentModel doc,
            List<String> fieldNames) throws ClientException {
        Map<String, String> davProps = new HashMap<String, String>();

        // auto add some compulsory properties
        if (!fieldNames.contains(WebDavConst.DAV_PROP_DISPLAYNAME)) {
            fieldNames.add(WebDavConst.DAV_PROP_DISPLAYNAME);
        }
        if (!fieldNames.contains(WebDavConst.DAV_PROP_RESOURCETYPE)) {
            fieldNames.add(WebDavConst.DAV_PROP_RESOURCETYPE);
        }
        if (!fieldNames.contains(WebDavConst.DAV_PROP_CONTENTTYPE)) {
            fieldNames.add(WebDavConst.DAV_PROP_CONTENTTYPE);
        }

        DavResourceAdapter adapter = doc.getAdapter(DavResourceAdapter.class);

        for (String prop : fieldNames) {
            String value = null;
            if (prop.equals(WebDavConst.DAV_PROP_DISPLAYNAME)) {
                if (adapter != null) {
                    value = adapter.getFileName();
                } else {
                    value = doc.getTitle();
                }
                if (value == null || "".equals(value)) {
                    value = doc.getName();
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_NAME)) {
                // FIXME: this is wrong
                value = doc.getTitle();
                value = doc.getName();

            } else if (prop.equals(WebDavConst.DAV_PROP_RESOURCETYPE)) {
                if (doc.isFolder()) {
                    value = MappingHelper.PROP_AS_TAG_PREFIX + "collection";
                } else {
                    value = doc.getType();
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_CONTENTTYPE)) {
                if (doc.isFolder()) {
                    continue; // XXX other wise webfolder will crash
                }
                if (adapter != null) {
                    value = adapter.getContentType();
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_ISCOLLECTION)) {
                if (doc.isFolder()) {
                    value = "1";
                } else {
                    value = "0";
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_ISROOT)) {
                if (doc.getType().equals("Domain")) {
                    value = "1";
                } else {
                    value = "0";
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_ISHIDDEN)) {
                if (doc.hasFacet("HIDDENINNAVIGATION")) {
                    value = "1";
                } else {
                    value = "0";
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_ISREADONLY)) {
                // XXX !!
                value = "0";
            } else if (prop.equals(WebDavConst.DAV_PROP_CREATIONDATE)) {
                Calendar created = (Calendar) doc.getProperty("dublincore",
                        "created");
                if (created != null) {
                    // value = created.getTime().toGMTString();
                    value = davDateFormat.format(created.getTime());
                } else {
                    // value= (new Date()).toGMTString();
                    value = davDateFormat.format(new Date());
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_LASTMODIFIED)) {
                Calendar modified = (Calendar) doc.getProperty("dublincore",
                        "modified");
                if (modified != null) {
                    value = davDateFormat2.format(modified.getTime());
                } else {
                    value = davDateFormat2.format(new Date());
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_LASTACCESSED)) {
                // return current time
                value = davDateFormat2.format(new Date());
                value = null;
            } else if (prop.equals(WebDavConst.DAV_PROP_CONTENTLANGUAGE)) {
                String lang = (String) doc.getProperty("dublincore", "language");
                if (lang == null || lang.equals("")) {
                    lang = "en-us";
                }
                value = lang;
            } else if (prop.equals(WebDavConst.DAV_PROP_PARENTNAME)) {
                try {
                    String path = doc.getPathAsString();
                    String[] pathParts = path.split("/");
                    value = pathParts[pathParts.length - 2];
                } catch (Throwable t) {
                    value = "";
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_CONTENTLENGTH)) {
                if (adapter != null) {
                    value = String.valueOf(adapter.getContentLength());
                }
            } else if (prop.equals(WebDavConst.DAV_PROP_LOCKDISCOVERY)) {
                // XXX
            } else if (prop.equals(WebDavConst.DAV_PROP_LOCKSCOPE)) {
                value = MappingHelper.PROP_AS_TAG_PREFIX + "exclusive";
            } else if (prop.equals(WebDavConst.DAV_PROP_LOCKTYPE)) {
                value = MappingHelper.PROP_AS_TAG_PREFIX + "write";
            } else if (prop.equals(WebDavConst.DAV_PROP_SUPPORTEDLOCK)) {
                value = "";
            }

            davProps.put(prop, value);
        }

        return davProps;
    }

    public Map<String, String> getAllDavProperties(DocumentModel doc) throws ClientException {
        return getDavProperties(doc, getDavPropertiesNames(doc));
    }

    public List<String> getDavPropertiesNames(DocumentModel doc) {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add(WebDavConst.DAV_PROP_CONTENTLANGUAGE); // X
        fieldNames.add(WebDavConst.DAV_PROP_CONTENTLENGTH); //X
        //fieldNames.add(WebDavConst.DAV_PROP_CONTENTTYPE);
        fieldNames.add(WebDavConst.DAV_PROP_CREATIONDATE); // MSWF : OK
        fieldNames.add(WebDavConst.DAV_PROP_DISPLAYNAME); // MSWF : OK
        //fieldNames.add(WebDavConst.DAV_PROP_ETAG);
        fieldNames.add(WebDavConst.DAV_PROP_LASTMODIFIED); //X
        //fieldNames.add(WebDavConst.DAV_PROP_LASTACCESSED); //X
        //fieldNames.add(WebDavConst.DAV_PROP_LOCKDISCOVERY);
        fieldNames.add(WebDavConst.DAV_PROP_RESOURCETYPE); // MSWF : OK
        //fieldNames.add(WebDavConst.DAV_PROP_LOCKSCOPE);
        //fieldNames.add(WebDavConst.DAV_PROP_LOCKTYPE);
        fieldNames.add(WebDavConst.DAV_PROP_SUPPORTEDLOCK); // MSWF : OK
        return fieldNames;
    }

}
