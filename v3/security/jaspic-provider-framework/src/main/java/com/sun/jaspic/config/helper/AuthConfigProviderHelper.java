/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jaspic.config.helper;

import delegate.*;
import java.util.HashSet;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigFactory.RegistrationContext;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;

/**
 *
 * @author Ron Monzillo
 */
public abstract class AuthConfigProviderHelper implements AuthConfigProvider {

    public static final String LAYER_NAME_KEY = "message.layer";
    public static final String ALL_LAYERS = "*";
    public static final String LOGGER_NAME_KEY = "logger.name";
    public static final String AUTH_MODULE_KEY = "auth.module.type";
    public static final String SERVER_AUTH_MODULE = "server.auth.module";
    public static final String CLIENT_AUTH_MODULE = "client.auth.module";
    HashSet<String> selfRegistered = new HashSet<String>();
    EpochCarrier providerEpoch = new EpochCarrier();

    protected final String getProperty(String key, String defaultValue) {
        String rvalue = defaultValue;
        Map<String, ?> properties = getProperties();
        if (properties != null && properties.containsKey(key)) {
            rvalue = (String) properties.get(key);
        }
        return rvalue;
    }

    protected String getLayer() {
        return getProperty(LAYER_NAME_KEY, ALL_LAYERS);
    }

    protected Class[] getModuleTypes() {
        Class[] rvalue = new Class[]{
            javax.security.auth.message.module.ServerAuthModule.class,
            javax.security.auth.message.module.ClientAuthModule.class
        };
        Map<String, ?> properties = getProperties();
        if (properties.containsKey(AUTH_MODULE_KEY)) {
            String keyValue = (String) properties.get(AUTH_MODULE_KEY);
            if (SERVER_AUTH_MODULE.equals(keyValue)) {
                rvalue = new Class[]{
                            javax.security.auth.message.module.ServerAuthModule.class
                        };
            } else if (CLIENT_AUTH_MODULE.equals(keyValue)) {
                rvalue = new Class[]{
                            javax.security.auth.message.module.ClientAuthModule.class
                        };
            }
        }
        return rvalue;
    }

    protected void selfRegister() {
        if (getFactory() != null) {
            selfRegistered.clear();
            RegistrationContext[] contexts = getSelfRegistrationContexts();
            for (RegistrationContext r : contexts) {
                String id = getFactory().registerConfigProvider(this,
                        r.getMessageLayer(), r.getAppContext(),
                        r.getDescription());
                selfRegistered.add(id);
            }
        }
    }

    protected CallbackHandler getClientCallbackHandler(CallbackHandler cbh)
            throws AuthException {
        if (cbh == null) {
            AuthException ae = new AuthException("AuthConfigProvider does not support null Client Callbackhandler");
            ae.initCause(new UnsupportedOperationException());
            throw ae;
        }
        return cbh;
    }

    protected CallbackHandler getServerCallbackHandler(CallbackHandler cbh) throws
            AuthException {
        if (cbh == null) {
            AuthException ae = new AuthException("AuthConfigProvider does not support null Server Callbackhandler");
            ae.initCause(new UnsupportedOperationException());
            throw ae;
        }
        return cbh;
    }

    public ClientAuthConfig getClientAuthConfig(String layer, String appContext,
            CallbackHandler cbh) throws AuthException {
        return new ClientAuthConfigHelper(getLoggerName(), providerEpoch,
                getAuthContextHelper(appContext,true),
                getMessagePolicyDelegate(appContext),
                layer, appContext,
                getClientCallbackHandler(cbh));
    }

    public ServerAuthConfig getServerAuthConfig(String layer, String appContext,
            CallbackHandler cbh) throws AuthException {
        return new ServerAuthConfigHelper(getLoggerName(), providerEpoch,
                getAuthContextHelper(appContext,true),
                getMessagePolicyDelegate(appContext),
                layer, appContext,
                getServerCallbackHandler(cbh));
    }

    public void refresh() {

        if (getFactory() != null) {
            String[] regID = getFactory().getRegistrationIDs(this);
            for (String i : regID) {
                if (selfRegistered.contains(i)) {
                    RegistrationContext c = getFactory().getRegistrationContext(i);
                    if (c != null && !c.isPersistent()) {
                        getFactory().removeRegistration(i);
                    }
                }
            }
        }
        providerEpoch.increment();
        selfRegister();
    }

    public String getLoggerName() {
        return getProperty(LOGGER_NAME_KEY, AuthConfigProviderHelper.class.getName());
    }

    public abstract Map<String, ?> getProperties();

    public abstract AuthConfigFactory getFactory();

    public abstract RegistrationContext[] getSelfRegistrationContexts();

    public abstract AuthContextHelper getAuthContextHelper(String appContext,
            boolean returnNullContexts) throws AuthException;

    public abstract MessagePolicyDelegate getMessagePolicyDelegate(String appContext) throws AuthException;

}

