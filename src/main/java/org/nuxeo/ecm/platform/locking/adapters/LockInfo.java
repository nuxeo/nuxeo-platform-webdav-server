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

package org.nuxeo.ecm.platform.locking.adapters;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Represents informations about a Nuxeo lock.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class LockInfo {

    private String userName;

    private Date lockDate;

    private String token;

    public LockInfo(String userName, String lockDateStr) throws ParseException {
        this.userName = userName;
        lockDate = DateFormat.getDateInstance(DateFormat.MEDIUM).parse(
                lockDateStr);
    }

    public LockInfo(String userName, Date lockDate) {
        this.userName = userName;
        this.lockDate = lockDate;
    }

    public Date getLockDate() {
        return lockDate;
    }

    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
