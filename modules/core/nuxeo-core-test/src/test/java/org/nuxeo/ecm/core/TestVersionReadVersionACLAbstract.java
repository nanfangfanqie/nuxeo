/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.BROWSE;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.core.security.SecurityService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
public abstract class TestVersionReadVersionACLAbstract {

    @Inject
    protected CoreFeature coreFeature;

    @Inject
    protected CoreSession session;

    @Inject
    protected SecurityService securityService;

    protected abstract boolean isReadVersionACLEnabled();

    @Test
    public void testReadVersionPermissionOnDocument() {
        doTestReadVersionPermission(true);
    }

    @Test
    public void testReadVersionPermissionOnFolder() {
        doTestReadVersionPermission(false);
    }

    protected void doTestReadVersionPermission(boolean aclOnDocument) {
        DocumentModel folder = session.createDocumentModel("/", "folder", "Folder");
        folder = session.createDocument(folder);

        DocumentModel file = session.createDocumentModel("/folder", "file", "File");
        file = session.createDocument(file);

        DocumentModel aclCarrier = aclOnDocument ? file : folder;
        ACP acp = new ACPImpl();
        acp.addACE("acl1", ACE.BLOCK);
        acp.addACE("acl1", new ACE("user1", "ReadVersion"));
        session.setACP(aclCarrier.getRef(), acp, true);

        // create a version
        DocumentRef verRef = session.checkIn(file.getRef(), VersioningOption.MINOR, null);
        String verId = session.getDocument(verRef).getId();

        // create a proxy pointing to the version
        session.createProxy(verRef, folder.getRef());
        session.save();
        coreFeature.waitForAsyncCompletion(); // DBS read ACL computation is async

        // check ACLs on the version
        acp = session.getACP(verRef);
        List<ACE> aces = acpToAces(acp);
        assertEquals(2, aces.size());
        assertEquals("user1", aces.get(0).getUsername());
        assertEquals(ACE.BLOCK, aces.get(1));

        // check Browse permission on the ACL
        assertCanBrowse(false, acp, "nosuchuser");
        assertCanBrowse(isReadVersionACLEnabled(), acp, "user1");

        // check Browse permission using CoreSession document API
        assertCanBrowse(false, verRef, "nosuchuser");
        assertCanBrowse(isReadVersionACLEnabled(), verRef, "user1");

        // check Browse permission using CoreSession query API
        assertCanQuery(false, verId, "nosuchuser");
        assertCanQuery(isReadVersionACLEnabled(), verId, "user1");

        // delete live document
        // the version stays because of the proxy (and orphan version removal is async anyway)
        session.removeDocument(file.getRef());
        session.save();
        coreFeature.waitForAsyncCompletion(); // DBS read ACL computation is async

        // check ACLs on the version
        acp = session.getACP(verRef);
        aces = acpToAces(acp);
        assertTrue(aces.isEmpty());

        // check Browse permission on the ACL
        assertCanBrowse(false, acp, "nosuchuser");
        assertCanBrowse(false, acp, "user1");

        // check Browse permission using CoreSession document API
        assertCanBrowse(false, verRef, "nosuchuser");
        assertCanBrowse(false, verRef, "user1");

        // check Browse permission using CoreSession query API
        assertCanQuery(false, verId, "nosuchuser");
        if (false) {
            // the current implementation cannot recompute read acls on versions when a live doc is deleted
            assertCanQuery(false, verId, "user1");
        }
    }

    protected static List<ACE> acpToAces(ACP acp) {
        return Arrays.stream(acp.getACLs()).flatMap(ACL::stream).collect(toList());
    }

    protected void assertCanBrowse(boolean expected, ACP acp, String user) {
        String[] browsePermissions = securityService.getPermissionsToCheck(BROWSE);
        assertEquals(expected, acp.getAccess(new String[] { user }, browsePermissions) == Access.GRANT);
    }

    protected void assertCanBrowse(boolean expected, DocumentRef docRef, String user) {
        CoreSession userSession = CoreInstance.getCoreSession(session.getRepositoryName(), user);
        assertEquals(expected, userSession.exists(docRef));
    }

    protected void assertCanQuery(boolean expected, String docId, String user) {
        CoreSession userSession = CoreInstance.getCoreSession(session.getRepositoryName(), user);
        DocumentModelList res = userSession.query(String.format("SELECT * FROM Document WHERE ecm:uuid = '%s'", docId));
        // first check the pure query, without accessing the document
        int size = res.size();
        assertTrue(String.valueOf(size), size <= 1);
        assertEquals(expected, size == 1);
    }

}
