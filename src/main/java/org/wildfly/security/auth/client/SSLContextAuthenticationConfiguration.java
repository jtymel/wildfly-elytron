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

package org.wildfly.security.auth.client;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.wildfly.security.SecurityFactory;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class SSLContextAuthenticationConfiguration extends AuthenticationConfiguration {

    private final SecurityFactory<SSLContext> sslContextFactory;

    SSLContextAuthenticationConfiguration(final AuthenticationConfiguration parent, final SSLContext sslContext) {
        this(parent, () -> sslContext);
    }

    SSLContextAuthenticationConfiguration(final AuthenticationConfiguration parent, final SecurityFactory<SSLContext> sslContextFactory) {
        super(parent);
        this.sslContextFactory = sslContextFactory;
    }

    AuthenticationConfiguration reparent(final AuthenticationConfiguration newParent) {
        return new SSLContextAuthenticationConfiguration(newParent, sslContextFactory);
    }

    SSLContext getSslContext() throws GeneralSecurityException {
        final SSLContext context = sslContextFactory.create();
        return context == null ? SSLContext.getDefault() : context;
    }
}
