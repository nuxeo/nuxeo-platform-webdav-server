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

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.repository.jcr.testing.RepositoryOSGITestCase;
import org.nuxeo.runtime.RuntimeService;

public abstract class AbstractTestRepository extends RepositoryOSGITestCase {

    protected CoreSession remote;

    protected RuntimeService runtime;

    public AbstractTestRepository(String name) {
        super(name);
    }

    protected DocumentModel getRootDocument() throws ClientException {
        return coreSession.getRootDocument();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // deploy("EventService.xml");
        // deploy("test-CoreExtensions.xml");
        // deploy("CoreTestExtensions.xml");

        openRepository();
        // compat with old test case
        remote = coreSession;
    }

}
