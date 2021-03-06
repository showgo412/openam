/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

package org.forgerock.openam.session.service.access.persistence;

import javax.inject.Inject;

import org.forgerock.openam.cts.exceptions.CoreTokenException;
import org.forgerock.util.Reject;

import com.iplanet.dpro.session.SessionID;
import com.iplanet.dpro.session.service.InternalSession;

/**
 * This class provides a bridge between session storage and the InternalSession persisting steps.
 */
public class InternalSessionPersistenceStoreStep implements InternalSessionStore {

    private SessionPersistenceStore sessionPersistenceStore;

    @Inject
    public InternalSessionPersistenceStoreStep(SessionPersistenceStore sessionPersistenceStore) {
        this.sessionPersistenceStore = sessionPersistenceStore;
    }

    @Override
    public InternalSession getBySessionID(SessionID sessionID) {
        Reject.ifNull(sessionID);
        return sessionPersistenceStore.recoverSession(sessionID);
    }

    @Override
    public InternalSession getByHandle(String sessionHandle) {
        Reject.ifNull(sessionHandle);
        return sessionPersistenceStore.recoverSessionByHandle(sessionHandle);
    }

    @Override
    public InternalSession getByRestrictedID(SessionID sessionID) {
        Reject.ifNull(sessionID);
        return sessionPersistenceStore.getByRestrictedID(sessionID);
    }

    @Override
    public void store(InternalSession session) throws SessionPersistenceException {
        Reject.ifNull(session);
        try {
            sessionPersistenceStore.save(session);
        } catch (CoreTokenException e) {
            throw new SessionPersistenceException("Failed to save session", e);
        }
    }

    @Override
    public void remove(SessionID sessionID) throws SessionPersistenceException {
        Reject.ifNull(sessionID);
        try {
            sessionPersistenceStore.delete(sessionID);
        } catch (CoreTokenException e) {
            throw new SessionPersistenceException("Failed to delete session", e);
        }
    }
}
