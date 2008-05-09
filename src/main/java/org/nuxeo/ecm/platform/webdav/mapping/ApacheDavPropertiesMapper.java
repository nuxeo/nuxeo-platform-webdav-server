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

package org.nuxeo.ecm.platform.webdav.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Properties mapper for Apache DAV properties.
 *
 * @author tiry
 */
public class ApacheDavPropertiesMapper implements PropertiesMapper {

    public Map<String, String> getAllDavProperties(DocumentModel doc) {
        return getDavProperties(doc, getDavPropertiesNames(doc));
    }

    public Map<String, String> getDavProperties(DocumentModel doc,
            List<String> fieldNames) {

        Map<String, String> result = new HashMap<String, String>();
        for (String fieldName : fieldNames) {
            if ("executable".equals(fieldName)) {
                result.put(fieldName, "F");
            }
        }
        return result;
    }

    public List<String> getDavPropertiesNames(DocumentModel doc) {
        List<String> names = new ArrayList<String>();
        names.add("executable");
        return names;
    }

}
