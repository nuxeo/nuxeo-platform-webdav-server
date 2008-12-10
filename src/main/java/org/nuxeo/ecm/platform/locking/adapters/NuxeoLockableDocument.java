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

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Lockable document adapter implementation.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class NuxeoLockableDocument implements LockableDocument {

    protected static final int LOCK_TIMEOUT_IN_HOURS = 36;

    protected final CoreSession session;

    protected final DocumentModel targetDoc;

    protected String existingLock;

    public NuxeoLockableDocument(DocumentModel doc) {
        session = getSessionFromDoc(doc);
        targetDoc = doc;
    }

    public boolean isLocked() throws ClientException {
        LockInfo info = getLockInfo();
        if (info == null || info.getUserName() == null) {
            return false;
        }

        return !isLockExpired();
    }

    protected static CoreSession getSessionFromDoc(DocumentModel doc) {
        return CoreInstance.getInstance().getSession(doc.getSessionId());
    }

    public boolean isLockExpired() throws ClientException {
        LockInfo lockInfo = getLockInfo();

        float lockAgeInHour = (new Date().getTime() - lockInfo.getLockDate().getTime())
                / (3600 * 1000);

        return lockAgeInHour > LOCK_TIMEOUT_IN_HOURS;
    }

    public LockInfo getLockInfo() throws ClientException {
        String existingLock = session.getLock(targetDoc.getRef());

        if (existingLock == null || existingLock.equals("")) {
            return null;
        }
        String[] info = existingLock.split(":");

        LockInfo lockInfo;
        try {
            lockInfo = new LockInfo(info[0], info[1]);
            lockInfo.setToken(existingLock);
        } catch (ParseException e) {
            throw new ClientException("Unable to parse lockInfo for document "
                    + targetDoc.getPathAsString(), e);
        }

        return lockInfo;
    }

    public int lock(String userName) throws ClientException {

        LockInfo lockInfo = getLockInfo();

        boolean canLock = false;

        int returnCode = LOCKED_OK;

        if (lockInfo != null) {
            // document is already locked
            String lockingUser = lockInfo.getUserName();

            if (lockingUser == null) {
                canLock = true;
            } else {
                if (lockingUser.equals(userName)) {
                    canLock = true;
                    return ALREADY_LOCKED_BY_YOU;
                } else {
                    if (isLockExpired()) {
                        canLock = true;
                        returnCode = LOCK_BORROWED;
                    } else {
                        canLock = false;
                        return CAN_NOT_BORROW_LOCK;
                    }
                }
            }
        }

        CoreSession session = getSessionFromDoc(targetDoc);
        session.setLock(targetDoc.getRef(), getLockToken(userName));
        session.save();

        return returnCode;
    }

    public int unlock(String userName) throws ClientException {

        LockInfo lockInfo = getLockInfo();

        boolean canUnLock = false;

        int returnCode = NOT_LOCKED;

        if (lockInfo != null) {
            // document is already locked
            String lockingUser = lockInfo.getUserName();

            if (lockingUser == null) {
                canUnLock = true;
                returnCode = NOT_LOCKED;
            } else {
                if (lockingUser.equals(userName)) {
                    canUnLock = true;
                    returnCode = ALREADY_LOCKED_BY_YOU;
                } else {
                    if (isLockExpired()) {
                        canUnLock = true;
                        returnCode = LOCK_EXPIRED;
                    } else {
                        canUnLock = false;
                        return CAN_NOT_UNLOCK;
                    }
                }
            }
        }

        if (canUnLock) {
            getSessionFromDoc(targetDoc).unlock(targetDoc.getRef());
        }

        return returnCode;
    }

    protected static String getLockToken(String user) {
        return user + ':'
                + DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date());
    }

}
