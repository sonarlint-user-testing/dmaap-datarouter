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
import javax.servlet.http.HttpServlet;
import org.onap.dmaap.datarouter.authz.Authorizer;
import org.onap.dmaap.datarouter.authz.impl.ProvAuthorizer;


/**
 * This is the base class for all Servlets in the provisioning code. It provides standard constants and some common
 * methods.
 *
 * @author Robert Eby
 * @version $Id: BaseServlet.java,v 1.16 2014/03/12 19:45:40 eby Exp $
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {


    public static final String LOGLIST_CONTENT_TYPE = "application/vnd.dmaap-dr.log-list; version=1.0";

    static final String OUTPUT_TYPE = "output_type";
    static final String REASON_SQL = "reasonSQL";

    /**
     * A boolean to trigger one time "provisioning changed" event on startup.
     */
    private static boolean startmsgFlag = true;

    /**
     * This logger is used to log provisioning events.
     */
    protected static EELFLogger eventlogger;
    /**
     * This logger is used to log internal events (errors, etc.)
     */
    protected static EELFLogger intlogger;
    /**
     * Authorizer - interface to the Policy Engine.
     */
    protected static Authorizer authz;

    /**
     * Initialize data common to all the provisioning server servlets.
     */
    protected BaseServlet() {
        setUpFields();
        if (authz == null) {
            authz = new ProvAuthorizer();
        }
        String name = this.getClass().getName();
        intlogger.info("PROV0002 Servlet " + name + " started.");
    }

    private static void setUpFields() {
        if (eventlogger == null) {
            eventlogger = EELFManager.getInstance().getLogger("EventLog");
        }
        if (intlogger == null) {
            intlogger = EELFManager.getInstance().getLogger("InternalLog");
        }
        if (startmsgFlag) {
            startmsgFlag = false;
        }
    }
}
