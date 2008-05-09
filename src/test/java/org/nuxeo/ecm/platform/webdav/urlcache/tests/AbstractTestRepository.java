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

package org.nuxeo.ecm.platform.webdav.urlcache.tests;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.runtime.RuntimeService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.TestRuntime;

public abstract class AbstractTestRepository extends TestCase {

    protected CoreSession remote;

    protected RuntimeService runtime;

    protected AbstractTestRepository(String name) {
        super(name);
    }

    public void openSession() throws ClientException {
        Map<String, Serializable> ctx = new HashMap<String, Serializable>();
        ctx.put("username", SecurityConstants.ADMINISTRATOR);
        remote = CoreInstance.getInstance().open("demo", ctx);

        assertNotNull(remote);
    }

    public void closeSession() throws ClientException {
        CoreInstance.getInstance().close(remote);
    }

    protected DocumentModel getRootDocument() throws ClientException {
        return remote.getRootDocument();
    }

    protected void cleanUp(DocumentRef ref) throws ClientException {
        remote.removeChildren(ref);
        remote.save();
    }

    @Override
    protected void setUp() throws Exception {
        // Duplicated from NXRuntimeTestCase
        runtime = Framework.getRuntime();
        if (runtime != null) {
            Framework.shutdown();
            runtime = null; // be sure no runtime is intialized (this may happen
                            // when some test crashes)
        }
        runtime = new TestRuntime();
        Framework.initialize(runtime);

        deploy("EventService.xml");

        deploy("CoreService.xml");
        deploy("TypeService.xml");
        deploy("SecurityService.xml");
        deploy("RepositoryService.xml");
        deploy("test-CoreExtensions.xml");
        deploy("CoreTestExtensions.xml");
        deploy("DemoRepository.xml");
        deploy("LifeCycleService.xml");
        deploy("LifeCycleServiceExtensions.xml");
        deploy("CoreEventListenerService.xml");
        deploy("DocumentAdapterService.xml");

        openSession();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Framework.shutdown();
    }

    // Duplicated from NXRuntimeTestCase
    public static URL getResource(String resource) {
        return Thread.currentThread().getContextClassLoader().getResource(
                resource);
    }

    // Duplicated from NXRuntimeTestCase
    public void deploy(String bundle) {
        URL url = getResource(bundle);
        assertNotNull("Test resource not found " + bundle, url);
        try {
            runtime.getContext().deploy(url);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to deploy bundle " + bundle);
        }
    }

    // Duplicated from NXRuntimeTestCase
    public void undeploy(String bundle) {
        URL url = getResource(bundle);
        assertNotNull("Test resource not found " + bundle, url);
        try {
            runtime.getContext().undeploy(url);
        } catch (Exception e) {
            fail("Failed to undeploy bundle " + bundle);
        }
    }

}
