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

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.dom.DOMCDATA;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;

/**
 * Helper class to handle DAV response XML generation.
 *
 * @author tiry
 */
public class DavResponseXMLHelper {

    public final QName multistatusTag = DocumentFactory.getInstance().createQName(
            "multistatus", WebDavConst.DAV_XML_PREFIX, WebDavConst.DAV_XML_URI);

    public final QName responseTag = DocumentFactory.getInstance().createQName(
            "response", WebDavConst.DAV_XML_PREFIX, WebDavConst.DAV_XML_URI);

    public final QName hrefTag = DocumentFactory.getInstance().createQName("href",
            WebDavConst.DAV_XML_PREFIX, WebDavConst.DAV_XML_URI);

    public final QName statusTag = DocumentFactory.getInstance().createQName(
            "status", WebDavConst.DAV_XML_PREFIX, WebDavConst.DAV_XML_URI);

    public final QName propstatTag = DocumentFactory.getInstance().createQName(
            "propstat", WebDavConst.DAV_XML_PREFIX, WebDavConst.DAV_XML_URI);

    public final QName propTag = DocumentFactory.getInstance().createQName("prop",
            WebDavConst.DAV_XML_PREFIX, WebDavConst.DAV_XML_URI);

    private Element root;

    private org.dom4j.Document rootDoc;

    public void initResponse() {
        if (root == null) {
            root = DocumentFactory.getInstance().createElement(multistatusTag);
            rootDoc = DocumentFactory.getInstance().createDocument(root);
        }
    }

    public void addSimpleResponse(String hrefText, String statusText) {
        initResponse();
        Element res = root.addElement(responseTag);
        Element href = res.addElement(hrefTag);
        href.setText(hrefText);
        Element status = res.addElement(statusTag);
        status.setText(statusText);
    }

    public String getAsXMLString() {
        initResponse();
        return rootDoc.asXML();
    }

    public Element getRootElement() {
        initResponse();
        return root;
    }

    public Map<String, Element> addResourceToResponse(String resourcesURL) {
        initResponse();

        Map<String, Element> result = new HashMap<String, Element>();

        Element res = root.addElement(responseTag);
        Element href = res.addElement(hrefTag);
        //if (!resourcesURL.startsWith("http"))
        //    resourcesURL="http://127.0.0.1:8080/nuxeo/dav/default" + resourcesURL;
        //String truncatedResourcesURL = resourcesURL.replace("http://192.168.1.111:8080", "");
        //truncatedResourcesURL = truncatedResourcesURL + "/";
        //href.setText(truncatedResourcesURL);
        href.setText(resourcesURL);

        // create the 200/OK propstatTag
        Element propsNode = res.addElement(propstatTag);
        // create the prop tag
        propsNode.addElement(propTag);
        Element statusElement = propsNode.addElement(statusTag);
        statusElement.setText("HTTP/1.1 " + WebDavConst.SC_OK);
        result.put(String.valueOf(WebDavConst.SC_OK), propsNode);

        // create the 404/OK propstatTag
        Element propsNode404 = res.addElement(propstatTag);
        // create the prop tag
        propsNode404.addElement(propTag);
        Element statusElement2 = propsNode404.addElement(statusTag);
        statusElement2.setText("HTTP/1.1 " + WebDavConst.SC_NOT_FOUND);
        result.put(String.valueOf(WebDavConst.SC_NOT_FOUND), propsNode404);

        return result;
    }

    public void addResourceStatusToResponse(Element propsNode, int status) {
        Element statusElement = propsNode.addElement(statusTag);
        statusElement.setText("HTTP/1.1 " + status);
    }

    public void addResourceStatusToResponse(Element propsNode, int status,
            String statusMessage) {
        Element statusElement = propsNode.addElement(statusTag);
        statusElement.setText("HTTP/1.1 " + status + ' ' + statusMessage);
    }

    public void addResourcePropertiesToResponse(String schemaURI,
            Map<String, String> props, Map<String, Element> propsNodes) {
        // get prefix for schemaURI
        String prefix = NameSpaceHelper.getNameSpacePrefix(schemaURI);

        // add NS alias
        Namespace ns = new Namespace(prefix, schemaURI);
        for (Element propsNode : propsNodes.values()) {
            propsNode.add(ns);
        }

        // get prop node
        Element prop = propsNodes.get(String.valueOf(WebDavConst.SC_OK)).element(propTag);
        Element prop404 = propsNodes.get(String.valueOf(WebDavConst.SC_NOT_FOUND)).element(
                propTag);

        for (String propName : props.keySet()) {
            QName qn = DocumentFactory.getInstance().createQName(propName,
                    prefix, schemaURI);

            String propValue = props.get(propName);
            if (propName.equals("supportedlock")) {
                // hard coded Lock config !!!

                Element tag = prop.addElement(qn);

                QName stqn = DocumentFactory.getInstance().createQName(
                        "lockentry", prefix, schemaURI);
                Element lentry = tag.addElement(stqn);

                QName stqn2 = DocumentFactory.getInstance().createQName(
                        "lockscope", prefix, schemaURI);
                Element lscope = lentry.addElement(stqn2);

                QName stqn3 = DocumentFactory.getInstance().createQName(
                        "exclusive", prefix, schemaURI);
                lscope.addElement(stqn3);

                QName stqn4 = DocumentFactory.getInstance().createQName(
                        "locktype", prefix, schemaURI);
                Element ltype = lentry.addElement(stqn4);

                QName stqn5 = DocumentFactory.getInstance().createQName(
                        "write", prefix, schemaURI);
                ltype.addElement(stqn5);

                continue;
            }

            if (propValue != null) {
                Element tag = prop.addElement(qn);
                if (propValue.startsWith(MappingHelper.PROP_AS_TAG_PREFIX)) {
                    String subTagName = propValue.split(":")[1];
                    QName stqn = DocumentFactory.getInstance().createQName(
                            subTagName, prefix, schemaURI);
                    tag.addElement(stqn);
                } else {
                    // if (propName.equals(WebDavConst.DAV_PROP_DISPLAYNAME))
                    if (propValue.contains("<") || propValue.contains(">")) {
                        tag.add(new DOMCDATA(propValue));
                    } else {
                        tag.setText(propValue);
                    }
                }
            } else {
                prop404.addElement(qn);
            }
        }
    }

    public void addResourceMissingPropertiesToResponse(String schemaURI,
            Map<String, String> props, Element propsNode) {
        // get prefix for schemaURI
        String prefix = NameSpaceHelper.getNameSpacePrefix(schemaURI);

        // add NS alias
        Namespace ns = new Namespace(prefix, schemaURI);
        propsNode.add(ns);

        // get prop node
        Element prop = propsNode.element(propTag);

        for (String propName : props.keySet()) {
            QName qn = DocumentFactory.getInstance().createQName(propName,
                    prefix, schemaURI);
            String propValue = props.get(propName);

            if (propValue == null) {
                Element tag = prop.addElement(qn);
            }
        }
    }

}
