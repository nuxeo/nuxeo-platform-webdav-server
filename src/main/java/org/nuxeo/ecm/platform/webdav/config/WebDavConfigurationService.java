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

package org.nuxeo.ecm.platform.webdav.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.webdav.adapters.DavResourceAdapter;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Runtime component to handle Extension points.
 *
 * @author tiry
 */
public class WebDavConfigurationService extends DefaultComponent {

    public static final String NAME = "org.nuxeo.ecm.platform.webdav.config.WebDavConfigurationService";

    public static final String CLIENT_CONFIGURATION_EP = "DavClientConfiguration";

    public static final String DAV_ADAPTER_EP = "DavAdapter";

    public static final String DEFAULT_CONFIGURATION = "default";

    private static final Log log = LogFactory.getLog(WebDavConfigurationService.class);

    private static Map<String, WebDavClientConfigurationDescriptor> clientConfigDescriptors;

    private static Map<String, DavAdapterDescriptor> davAdapterDescriptors;

    @Override
    public void activate(ComponentContext context) {
        clientConfigDescriptors = new HashMap<String, WebDavClientConfigurationDescriptor>();
        davAdapterDescriptors = new HashMap<String, DavAdapterDescriptor>();
    }

    @Override
    public void deactivate(ComponentContext context) {
        clientConfigDescriptors = null;
        davAdapterDescriptors = null;
    }

    private static void mergeClientDescriptors(
            WebDavClientConfigurationDescriptor newContrib) {
        WebDavClientConfigurationDescriptor oldDescriptor = clientConfigDescriptors.get(newContrib.getName());

        // Enable/Disable
        if (newContrib.getEnabled() != null) {
            oldDescriptor.setEnabled(newContrib.getEnabled());
        }
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {
        if (extensionPoint.equals(CLIENT_CONFIGURATION_EP)) {
            WebDavClientConfigurationDescriptor config = (WebDavClientConfigurationDescriptor) contribution;
            registerClientConfig(config, contributor);
        } else if (extensionPoint.equals(DAV_ADAPTER_EP)) {
            DavAdapterDescriptor davAdapter = (DavAdapterDescriptor) contribution;
            registerDavAdapter(davAdapter, contributor);
        } else {
            log.error("Extension point " + extensionPoint + "is unknown");
        }
    }

    public void registerClientConfig(
            WebDavClientConfigurationDescriptor config,
            ComponentInstance contributor) {
        if (clientConfigDescriptors.containsKey(config.getName())) {
            mergeClientDescriptors(config);
            log.debug("merged WebDav client configuration descriptor: "
                    + config.getName());
        } else {
            clientConfigDescriptors.put(config.getName(), config);
            log.debug("registered WebDav client configuration descriptor: "
                    + config.getName());
        }
    }

    public void registerDavAdapter(DavAdapterDescriptor davAdapter,
            ComponentInstance contributor) {
        davAdapterDescriptors.put(davAdapter.getTypeName(), davAdapter);
        log.debug("registered WebDav Adapterdescriptor: "
                + davAdapter.getName());
    }

    public WebDavClientConfigurationDescriptor getClientConfig(String UserAgent) {
        if (UserAgent == null || "".equals(UserAgent.trim())) {
            return clientConfigDescriptors.get(DEFAULT_CONFIGURATION);
        }

        for (String configName : clientConfigDescriptors.keySet()) {
            if (configName.equals(DEFAULT_CONFIGURATION)) {
                continue;
            }

            for (String pattern : clientConfigDescriptors.get(configName).getUaPatterns()) {
                if (UserAgent.contains(pattern)) {
                    return clientConfigDescriptors.get(configName);
                }
            }
        }

        return clientConfigDescriptors.get(DEFAULT_CONFIGURATION);
    }

    public DavResourceAdapter getAdapterForType(String docType)
            throws InstantiationException, IllegalAccessException {
        DavAdapterDescriptor dad = davAdapterDescriptors.get(docType);
        if (dad != null) {
            return dad.getNewInstance();
        } else {
            return null;
        }
    }

}
