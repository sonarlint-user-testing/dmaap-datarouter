/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dmaap.datarouter.provisioning.utils;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AafPropsUtils {

    private static EELFLogger eelfLogger = EELFManager.getInstance().getLogger(AafPropsUtils.class);

    public static final String KEYSTORE_TYPE_PROPERTY = "PKCS12";
    public static final String TRUESTSTORE_TYPE_PROPERTY = "jks";
    private static final String KEYSTORE_PATH_PROPERTY = "cadi_keystore";
    private static final String KEYSTORE_PASS_PROPERTY = "cadi_keystore_password_p12";
    private static final String TRUSTSTORE_PATH_PROPERTY = "cadi_truststore";
    private static final String TRUSTSTORE_PASS_PROPERTY = "cadi_truststore_password";


    public AafPropsUtils(File propsFile) throws IOException {
    }

    private String decryptedPass(String password) {
        String decryptedPass = null;
        return decryptedPass;
    }

    public String getKeystorePathProperty() {
        return "";
    }

    public String getKeystorePassProperty() {
        return "";
    }

    public String getTruststorePathProperty() {
        return "";
    }

    public String getTruststorePassProperty() {
        return decryptedPass("");
    }

}
