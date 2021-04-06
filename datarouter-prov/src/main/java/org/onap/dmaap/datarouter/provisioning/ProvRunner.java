/*******************************************************************************
 * ============LICENSE_START==================================================
 * * org.onap.dmaap
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * *
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/


package org.onap.dmaap.datarouter.provisioning;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.eclipse.jetty.server.Server;

import static java.lang.System.exit;
import static java.lang.System.getProperty;

public class ProvRunner {

    public static final EELFLogger intlogger = EELFManager.getInstance()
                                                       .getLogger("org.onap.dmaap.datarouter.provisioning.internal");

    private static Properties provProperties;

    /**
     * Starts the Data Router Provisioning server.
     *
     * @param args not used
     */
    public static void main(String[] args) {

        try {
            // Create and start the Jetty server
            Server provServer = ProvServer.getServerInstance();
            intlogger.info("PROV0000 **** DMaaP Data Router Provisioning Server starting....");
            provServer.start();
            provServer.dumpStdErr();
            provServer.join();
            intlogger.info("PROV0000 **** DMaaP Data Router Provisioning Server started: " + provServer.getState());
        } catch (Exception e) {
            intlogger.error(
                "PROV0010 **** DMaaP Data Router Provisioning Server failed to start. Exiting: " + e.getMessage(), e);
            exit(1);
        }
    }

    public static Properties getProvProperties() {
        if (provProperties == null) {
            try {
                provProperties = new Properties();
                provProperties.load(new FileInputStream(getProperty(
                    "org.onap.dmaap.datarouter.provserver.properties",
                    "/opt/app/datartr/etc/provserver.properties")));
            } catch (IOException e) {
                intlogger.error("Failed to load PROV properties: " + e.getMessage(), e);
                exit(1);
            }
        }
        return provProperties;
    }
}
