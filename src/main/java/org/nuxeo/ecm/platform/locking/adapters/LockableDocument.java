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

import org.nuxeo.ecm.core.api.ClientException;

/**
* Interface of the DocumentModel adapter for a lockable doc.
*
* @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
*/
public interface LockableDocument {

    int LOCKED_OK = 0;

    int ALREADY_LOCKED_BY_YOU = 1;

    int LOCK_BORROWED = 2;

    int CAN_NOT_BORROW_LOCK = -1;

    int CAN_NOT_LOCK = -2;

    int UNLOCKED_OK = 10;

    int NOT_LOCKED = 0;

    int CAN_NOT_UNLOCK = -11;

    int LOCK_EXPIRED = 11;

    /**
     * Returns true if document is locked.
     *
     * @return true if document is locked
     * @throws ClientException
     */
    boolean isLocked() throws ClientException;

    /**
     * Creates an exclusive lock on the document.
     *
     * @param userName
     * @return LOCKED_OK if lock succeeds,
     *         ALREADY_LOCKED_BY_YOU if the document was already locked by userName,
     *         LOCK_BORROWED if lock was borrowed from another user,
     *         CAN_NOT_BORROW_LOCK if document if already locked by another user and lock can not be borrowed,
     *         CAN_NOT_LOCK if lock can't not be done (permission)
     * @throws ClientException
     */
    int lock(String userName) throws ClientException;

    /**
     * Unlocks a document.
     *
     * @param userName
     * @return UNLOCKED_OK if unlock succeeds, NOT_LOCKED if the document was not
     *         locked, CAN_NOT_UNLOCK if unlock can not be done
     * @throws ClientException
     */
    int unlock(String userName) throws ClientException;

    /**
     * Returns true if lock is expired.
     *
     * @return true if lock is expired
     * @throws ClientException
     */
    boolean isLockExpired() throws ClientException;

    /**
     * Returns lock information or null if no lock exists.
     *
     * @return lock information or null if no lock exists
     * @throws ClientException
     */
    LockInfo getLockInfo() throws ClientException;

}
