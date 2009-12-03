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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.ServletException;

import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.Node;
import org.nuxeo.ecm.platform.webdav.mapping.NuxeoComplexTypeExtractor;
import org.nuxeo.ecm.platform.webdav.servlet.WebDavConst;
import org.nuxeo.ecm.platform.webdav.urlcache.URLResolverCache;

//
// FIXME: these tests are deactivated for now (by making this class abstract),
// because the XML property export doesn't work properly yet.
// This is mostly harmless, as no popular WebDAV client implements this either.
//
public abstract class TestNuxeoDavProperties extends AbstractWebDavRequestTestCase {

    private static final String DESCRIPTION = "This is a test description";
    private static final String SOURCE = "This is a test source";

    private static final Calendar created = new GregorianCalendar();
    private static final Calendar modified = new GregorianCalendar();

    private static final String DC_SCHEMA_PREFIX = "dc";
    private static final String DC_SCHEMA_URL = "http://www.nuxeo.org/dublincore/";

    private static final QName createdTag = DocumentFactory.getInstance().createQName(
            "created", DC_SCHEMA_PREFIX, DC_SCHEMA_URL);
    private static final QName modifiedTag = DocumentFactory.getInstance().createQName(
            "modified", DC_SCHEMA_PREFIX, DC_SCHEMA_URL);
    private static final QName descriptionTag = DocumentFactory.getInstance().createQName(
            "description", DC_SCHEMA_PREFIX, DC_SCHEMA_URL);
    private static final QName sourceTag = DocumentFactory.getInstance().createQName(
            "source", DC_SCHEMA_PREFIX, DC_SCHEMA_URL);
    private static final QName contributorsTag = DocumentFactory.getInstance().createQName(
            "contributors", DC_SCHEMA_PREFIX, DC_SCHEMA_URL);

    private final List<String> contributors = new ArrayList<String>();

    protected TestNuxeoDavProperties(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        folder3.setProperty("dublincore", "description", DESCRIPTION);
        folder3.setProperty("dublincore", "source", SOURCE);
        // Dublincore listener is not deployed in test setup => set this by hand
        folder3.setProperty("dublincore", "created", created);
        folder3.setProperty("dublincore", "modified", modified);

        contributors.add("bitou1");
        contributors.add("bitou2");
        contributors.add("bitou3");
        folder3.setProperty("dublincore", "contributors", contributors);

        remote.saveDocument(folder3);
        remote.save();

        URLResolverCache.resetCache();
    }

    public void testSimpleNuxeoProperties() throws IOException,
            ServletException, DocumentException {
        //System.out.println(WebDavConst.HTTP_HEADER_DATE_FORMAT.format(modified.getTime()));

        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        assertNotNull(in);
        FakeRequest fReq = new FakeRequest("PROPFIND",
                "/demo/Folder1/Folder2/Folder3", in);

        fReq.addHeader("Depth", "0");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(1, s);

        String xmlStr = xml.asXML();
        //System.out.print("XML to check: " + xmlStr);
        assertTrue(xmlStr.contains("dc:created"));

        Element response = (Element) xml.elements("response").get(0);
        Element propstat = (Element) response.elements("propstat").get(0);
        Element prop = (Element) propstat.elements("prop").get(0);

        assertEquals(1, prop.elements(createdTag).size());
        assertEquals(1, prop.elements(modifiedTag).size());

        assertEquals(DESCRIPTION,
                ((Node) prop.elements(descriptionTag).get(0)).getText());
        assertEquals(SOURCE,
                ((Node) prop.elements(sourceTag).get(0)).getText());
    }

    public void testComplexNuxeoProperties() throws IOException,
            ServletException, DocumentException {
        InputStream in = getResource("xml-dumps/nxprops.xml").openStream();
        assertNotNull(in);

        FakeRequest fReq = new FakeRequest("PROPFIND",
                "/demo/Folder1/Folder2/Folder3", in);
        fReq.addHeader("Depth", "0");

        FakeResponse res = execDavRequest(fReq);
        assertEquals(WebDavConst.SC_MULTI_STATUS, res.getStatus());

        Element xml = res.getXMLOutput();
        int s = xml.elements("response").size();
        assertEquals(1, s);

        String xmlStr = xml.asXML();
        //System.out.print("XML to check: " + xmlStr);

        Element response = (Element) xml.elements("response").get(0);
        Element propstat = (Element) response.elements("propstat").get(0);
        Element prop = (Element) propstat.elements("prop").get(0);

        assertEquals(((Node) prop.elements(contributorsTag).get(0)).getText(),
                contributors.toString());
    }

    public void testXMLComplexProperties() {
        NuxeoComplexTypeExtractor extractor = new NuxeoComplexTypeExtractor(
                folder3);
        Element contribs = extractor.extractProperty("dublincore", "contributors");
        assertEquals(3, contribs.elements("item").size());
    }

}
