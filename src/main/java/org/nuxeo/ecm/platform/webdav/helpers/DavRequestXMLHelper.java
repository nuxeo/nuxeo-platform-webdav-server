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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.nuxeo.ecm.platform.locking.adapters.DavLockInfo;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;

/**
 * Helper to handle DAV requests XML parsing.
 *
 * @author tiry
 */
public class DavRequestXMLHelper {

    // Utility class.
    private DavRequestXMLHelper() {
    }

    @SuppressWarnings("unchecked")
    public static DavLockInfo extractLockInfo(Element root) {

        DavLockInfo lockInfo = new DavLockInfo();

        if (root == null) {
            return lockInfo;
        }

        Iterator<Element> eiter = root.elementIterator();

        while (eiter.hasNext()) {
            Element child = eiter.next();
            String tagName = child.getName();
            String uri = child.getNamespace().getURI();
            if (uri.equals(WebDavConst.DAV_XML_URI) && tagName.equals("lockinfo")) {
                Iterator<Element> citer = child.elementIterator();
                while (citer.hasNext()) {
                    Element subChild = citer.next();
                    if (uri.equals(WebDavConst.DAV_XML_URI) && tagName.equals("owner")) {
                        if (subChild.nodeCount() > 0) {
                            lockInfo.setOwner(subChild.getText());
                        } else {
                            lockInfo.setOwner(subChild.node(0).getStringValue());
                        }
                    }
                }
                return lockInfo;
            }
        }

        return lockInfo;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, List<String>> extractProperties(Element root) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();

        if (root == null) {
            result.put("*", null);
            return result;
        }

        Iterator<Element> eiter = root.elementIterator();

        while (eiter.hasNext()) {
            Element child = eiter.next();
            String tagName = child.getName();
            String uri = child.getNamespace().getURI();
            if (uri.equals(WebDavConst.DAV_XML_URI) && tagName.equals("prop")) {
                extractProperty(child, result);
            } else if (uri.equals(WebDavConst.DAV_XML_URI) && tagName.equals("allprop")) {
                result.put("*", null);
                return result;
            } else if (uri.equals(WebDavConst.DAV_XML_URI) && tagName.equals("propname")) {
                result.put("propname", null);
                return result;
            }

        }
        //System.out.print(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void extractProperty(Element prop,
            Map<String, List<String>> result) {
        Iterator<Element> eiter = prop.elementIterator();
        while (eiter.hasNext()) {
            Element child = eiter.next();
            String tagName = child.getName();
            String prefix = child.getNamespace().getPrefix();
            String uri = child.getNamespace().getURI();

            // store in order to use the same prefix in response
            NameSpaceHelper.addNameSpace(uri, prefix);

            if (result.containsKey(uri)) {
                List<String> fields = result.get(uri);
                if (fields == null) {
                    fields = new ArrayList<String>();
                }
                fields.add(tagName);
                result.put(uri, fields);
            } else {
                List<String> fields = new ArrayList<String>();
                fields.add(tagName);
                result.put(uri, fields);
            }
        }
    }

}
