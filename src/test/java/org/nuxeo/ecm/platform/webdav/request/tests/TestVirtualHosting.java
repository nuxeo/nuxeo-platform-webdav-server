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
import java.util.List;

import javax.servlet.ServletException;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;

public class TestVirtualHosting extends AbstractWebDavRequestTestCase {

    public TestVirtualHosting(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    public void testXForwardedHost() throws IOException, ServletException,
            DocumentException {
        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        FakeRequest fReq = new FakeRequest("PROPFIND", "/demo/Folder1/Folder2",
                in);

        fReq.addHeader("Depth", "infinity");
        fReq.addHeader("needFullURLs", "Y");
        fReq.addHeader(WebDavConst.X_FORWARDED_HOST, "MyApacheServer");

        FakeResponse res = execDavRequest(fReq);

        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(4, s);

        List<Element> responses = xml.elements("response");

        for (Element response : responses) {
            Element href = (Element) response.elements("href").get(0);
            assertNotNull(href);
            assertTrue(href.getText().startsWith("http://MyApacheServer"));
        }
    }

    @SuppressWarnings("unchecked")
    public void testNuxeoVH() throws IOException, ServletException,
            DocumentException {
        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        FakeRequest fReq = new FakeRequest("PROPFIND", "/demo/Folder1/Folder2",
                in);

        fReq.addHeader("Depth", "infinity");
        fReq.addHeader("needFullURLs", "Y");
        fReq.addHeader(WebDavConst.VH_HEADER, "https://MyApacheServer");

        FakeResponse res = execDavRequest(fReq);

        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(4, s);

        List<Element> responses = xml.elements("response");

        for (Element response : responses) {
            Element href = (Element) response.elements("href").get(0);
            assertNotNull(href);
            assertTrue(href.getText().startsWith(
                    "https://MyApacheServer/nuxeo/dav/"));
        }
    }

    @SuppressWarnings("unchecked")
    public void testNuxeoVH2() throws IOException, ServletException,
            DocumentException {
        URLResolverCache.resetCache();

        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        FakeRequest fReq = new FakeRequest("PROPFIND", "/demo/Folder1/Folder2",
                in);

        fReq.addHeader("Depth", "infinity");
        fReq.addHeader("needFullURLs", "Y");
        fReq.addHeader(WebDavConst.VH_HEADER, "https://MyApacheServer/");

        FakeResponse res = execDavRequest(fReq);

        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(4, s);

        List<Element> responses = xml.elements("response");

        for (Element response : responses) {
            Element href = (Element) response.elements("href").get(0);
            assertNotNull(href);
            assertTrue(href.getText().startsWith(
                    "https://MyApacheServer/nuxeo/dav/"));
        }
    }

}
