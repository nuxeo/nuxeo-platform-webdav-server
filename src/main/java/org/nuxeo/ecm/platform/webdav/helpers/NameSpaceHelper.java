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
import java.util.Map;

import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;


/**
 * Helper to manage namespace aliases.
 *
 * @author tiry
 */
public class NameSpaceHelper {

    private static final Map<String, String> nsCache = new HashMap<String, String>();

    private static int generatedSerialCounter = 0;

    // Utility class.
    private NameSpaceHelper() {
    }

    public static void addNameSpace(String uri, String prefix) {
        if (prefix != null && !prefix.equals("") && !nsCache.containsKey(uri)) {
            nsCache.put(uri, prefix);
        }
    }

    public static String getNameSpacePrefix(String uri) {
        if (nsCache.containsKey(uri)) {
            return nsCache.get(uri);
        }

        String generatedPrefix = generatePrefix(uri);
        addNameSpace(uri, generatedPrefix);
        return generatedPrefix;
    }

    private static String generatePrefix(String uri) {
        if (uri.startsWith(WebDavConst.NUXEO_SCHEMA_NS)) {
            String[] parts = uri.split("/");
            String lastToken = parts[parts.length - 1];
            if (lastToken.equals("")) {
                lastToken = parts[parts.length - 2];
            }
            return lastToken;
        } else if (uri.equals(WebDavConst.DAV_XML_URI)) {
            return WebDavConst.DAV_XML_PREFIX;
        } else {
            generatedSerialCounter += 1; // XXX concurrency !!
            return "NS" + generatedSerialCounter;
        }
    }

}
