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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.DataModel;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.DocumentPart;

/**
 * Properties mapper for Nuxeo custom properties.
 *
 * @author tiry
 */
public class SchemaPropertiesMapper implements PropertiesMapper {

    private String schema;

    public SchemaPropertiesMapper() {
        schema = null;
    }

    public SchemaPropertiesMapper(String uri) {
        schema = uri.split("/")[3];
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getDavProperties(DocumentModel doc,
            List<String> fieldNames) {

        if (schema == null) {
            return null;
        }

        DataModel dataModel = doc.getDataModel(schema);
        Map<String, Object> data = dataModel.getMap();

        Map<String, String> result = new HashMap<String, String>();

        for (String k : fieldNames) {
            if (data.keySet().contains(k)) {
                Object value = data.get(k);

                if (value != null) {
                    String strValue;
                    if (value instanceof String) {
                        strValue = (String) value;
                    } else if (value instanceof Date) {
                        strValue = ((Date) value).toGMTString();
                    } else if (value instanceof Calendar) {
                        strValue = ((Calendar) value).getTime().toGMTString();
                    } else if (value instanceof String[]) {
                        strValue = Arrays.toString((String[]) value);
                    } else if (value instanceof List) {
                        List<?> lstValues = (List) value;

                        strValue = "[";
                        for (Object val : lstValues) {
                            if (val instanceof String) {
                                strValue += (String) val + ',';
                            } else {
                                strValue += val.toString() + ',';
                            }
                        }
                        if (strValue.endsWith(",")) {
                            strValue = strValue.substring(0,
                                    strValue.length() - 1);
                        }
                        strValue += ']';
                    } else {
                        strValue = value.toString();
                    }
                    result.put(k, strValue);
                }
            }
        }
        return result;
    }

    public Map<String, String> getAllDavProperties(DocumentModel doc) {
        if (schema == null) {
            return null;
        }
        return getDavProperties(doc, getDavPropertiesNames(doc));
    }

    public List<String> getDavPropertiesNames(DocumentModel doc) {
        if (schema == null) {
            return null;
        }

        List<String> names = new ArrayList<String>();
        DocumentPart part = doc.getPart(schema);
        for (org.nuxeo.ecm.core.api.model.Property prop : part.getChildren())
        {
            names.add(prop.getName());
        }
        return names;
        /*DataModel dataModel = doc.getDataModel(schema);
        Map<String, Object> data = dataModel.getMap();

        List<String> fieldNames = new ArrayList<String>();
        fieldNames.addAll(data.keySet());

        return fieldNames;*/
    }

}
