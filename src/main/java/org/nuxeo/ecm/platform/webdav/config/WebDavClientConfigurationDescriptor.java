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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * Descriptor for DAV client configuration.
 *
 * @author tiry
 */
@XObject(value="davClientConfiguration")
public class WebDavClientConfigurationDescriptor implements Serializable {

    private static final long serialVersionUID = 7135358405707233912L;

    @XNode("@name")
    private String name;

    @XNode("@enabled")
    private Boolean enabled = true;

    @XNodeList(value = "userAgentPatterns/pattern", type = ArrayList.class, componentType = String.class)
    private List<String> uaPatterns = new ArrayList<String>();

    @XNode("needGetParameterForCollectionNamming")
    private boolean needGetParameterForCollectionNamming = false;

    @XNode("needVirtualPathForLief")
    private boolean needVirtualPathForLief = false;

    @XNode("useFileNameAsRessourceName")
    private boolean useFileNameAsRessourceName = false;

    @XNode("needFullURLs")
    private boolean needFullURLs = false;

    @XNode("needMSDavHeader")
    private boolean needMSDavHeader = false;

    @XNode("skipLevel0ForListing")
    private boolean skipLevel0ForListing = false;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUaPatterns() {
        return uaPatterns;
    }

    public void setUaPatterns(List<String> uaPatterns) {
        this.uaPatterns = uaPatterns;
    }

    public boolean getNeedFullURLs() {
        return needFullURLs;
    }

    public void setNeedFullURLs(boolean needFullURLs) {
        this.needFullURLs = needFullURLs;
    }

    public boolean getNeedGetParameterForCollectionNamming() {
        return needGetParameterForCollectionNamming;
    }

    public void setNeedGetParameterForCollectionNamming(
            boolean needGetParameterForCollectionNamming) {
        this.needGetParameterForCollectionNamming = needGetParameterForCollectionNamming;
    }

    public boolean getNeedMSDavHeader() {
        return needMSDavHeader;
    }

    public void setNeedMSDavHeader(boolean needMSDavHeader) {
        this.needMSDavHeader = needMSDavHeader;
    }

    public boolean getNeedVirtualPathForLief() {
        return needVirtualPathForLief;
    }

    public void setNeedVirtualPathForLief(boolean needVirtualPathForLief) {
        this.needVirtualPathForLief = needVirtualPathForLief;
    }

    public boolean getUseFileNameAsRessourceName() {
        return useFileNameAsRessourceName;
    }

    public void setUseFileNameAsRessourceName(boolean useFileNameAsRessourceName) {
        this.useFileNameAsRessourceName = useFileNameAsRessourceName;
    }

    public boolean getSkipLevel0ForListing() {
        return skipLevel0ForListing;
    }

    public void setSkipLevel0ForListing(boolean skipLevel0ForListing) {
        this.skipLevel0ForListing = skipLevel0ForListing;
    }

}
