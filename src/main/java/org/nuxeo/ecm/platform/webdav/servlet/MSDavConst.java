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

package org.nuxeo.ecm.platform.webdav.servlet;

/**
 * Constants for MS DAV properties.
 *
 * @author tiry
 */
public class MSDavConst {

    // MSO http://schemas.stylusstudio.com/msoffice2003/nd58d9dfe/index.html
    public static final String MS_DAV_MSO_NS = "urn:schemas-microsoft-com:office:office";
    public static final String MS_DAV_MSO_PREFIX = "j";

    // SP !
    public static final String MS_DAV_PUB_NS = "urn:schemas-microsoft-com:publishing:";
    public static final String MS_DAV_PUB_PREFIX = "k";

    // Exchange ?
    public static final String MS_DAV_MAPI_NS = "http: //schemas.microsoft.com/mapi/proptag/";
    public static final String MS_DAV_MAPI_PREFIX = "l";

    public static final String MS_DAV_URL_DELIMITER = "WebFolder_FileName";

    /*
    + " <j: Title/> "
    + " <j: Author/> "
    + " <j: Description/> "
    + " <j: Keywords/> "
    + " <k: BaseDoc/> "
    + " <k: Categories/> "
    + " <k: documentstate/> "
    + " <k: hasbeenapproved/> "
    + " <k: operationsallowed/> "
    + " <k: WorkingCopy/> "
    + " <k: checkedoutby/> "
    + " <k: LastUnapprovedVersion/> "
    + " <k: FriendlyVersionID/> "
    + " <k: IsCheckedOut/> "
    + " <k: currentapprovers/> "
    + " <k: savedapproversleft/> "
    + " <k: IsHiddenInPortal/> "
    */

    // Constant utility class.
    private MSDavConst() {
    }

}
