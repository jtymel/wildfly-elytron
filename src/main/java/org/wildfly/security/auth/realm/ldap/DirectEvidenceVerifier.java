/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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
 */
package org.wildfly.security.auth.realm.ldap;

import static org.wildfly.security._private.ElytronMessages.log;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.LdapContext;

import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SupportLevel;
import org.wildfly.security.evidence.Evidence;
import org.wildfly.security.evidence.PasswordGuessEvidence;

/**
 * An {@link EvidenceVerifier} that verifies a guess by using it to connect to LDAP.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
class DirectEvidenceVerifier implements EvidenceVerifier {

    private static final DirectEvidenceVerifier INSTANCE = new DirectEvidenceVerifier();

    private DirectEvidenceVerifier() {
    }

    static DirectEvidenceVerifier getInstance() {
        return INSTANCE;
    }

    @Override
    public SupportLevel getEvidenceVerifySupport(final DirContext context, final Class<? extends Evidence> evidenceType, final String algorithmName) throws RealmUnavailableException {
        return evidenceType == PasswordGuessEvidence.class && context instanceof LdapContext ? SupportLevel.SUPPORTED : SupportLevel.UNSUPPORTED;
    }

    @Override
    public IdentityEvidenceVerifier forIdentity(final DirContext dirContext, final String distinguishedName) throws RealmUnavailableException {
        return new IdentityEvidenceVerifier() {
            @Override
            public SupportLevel getEvidenceVerifySupport(final Class<? extends Evidence> evidenceType, final String algorithmName) throws RealmUnavailableException {
                return evidenceType == PasswordGuessEvidence.class ? SupportLevel.SUPPORTED : SupportLevel.UNSUPPORTED;
            }

            @Override
            public boolean verifyEvidence(Evidence evidence) throws RealmUnavailableException {
                if (evidence instanceof PasswordGuessEvidence) {
                    char[] password = ((PasswordGuessEvidence) evidence).getGuess();

                    try {
                        LdapContext userContext = ((LdapContext) dirContext).newInstance(null);
                        userContext.addToEnvironment(InitialDirContext.SECURITY_PRINCIPAL, distinguishedName);
                        userContext.addToEnvironment(InitialDirContext.SECURITY_CREDENTIALS, password);
                        userContext.reconnect(null);
                        userContext.close();
                        return true;
                    } catch (NamingException e) {
                        log.debugf("Credential direct evidence verification failed. DN: [%s]", distinguishedName, e);
                    } finally {
                        ((PasswordGuessEvidence) evidence).destroy();
                    }
                }

                return false;
            }

        };
    }

}
