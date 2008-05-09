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
 * $Id$
 */

package org.nuxeo.ecm.platform.webdav.request.tests;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;

public class TestDavPropfind extends AbstractWebDavRequestTestCase {

    public TestDavPropfind(String name) {
        super(name);
    }

    public void testPropFindAllProps() throws IOException, ServletException,
            DocumentException {
        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        FakeRequest fReq = new FakeRequest("PROPFIND",
                "/demo/Folder1/Folder2/Folder3", in);

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(3, s);
    }

    public void testPropFindAllPropsWithSlash() throws IOException, ServletException,
            DocumentException {
        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        FakeRequest fReq = new FakeRequest("PROPFIND",
                "/demo/Folder1/Folder2/Folder3/", in);

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(3, s);
    }

    public void testDepth0() throws IOException, ServletException,
            DocumentException {
        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        FakeRequest fReq = new FakeRequest("PROPFIND",
                "/demo/Folder1/Folder2/Folder3", in);

        fReq.addHeader("Depth", "0");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(1, s);
    }

    public void testDepth1() throws IOException, ServletException,
            DocumentException {
        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        FakeRequest fReq = new FakeRequest("PROPFIND",
                "/demo/Folder1/Folder2/Folder3", in);

        fReq.addHeader("Depth", "1");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(3, s);
    }

    public void testDepthInfinity() throws IOException, ServletException,
            DocumentException {
        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        FakeRequest fReq = new FakeRequest("PROPFIND", "/demo/Folder1/Folder2",
                in);

        fReq.addHeader("Depth", "infinity");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(4, s);
    }

}
