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

package org.onap.dmaap.datarouter.authz.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.onap.dmaap.datarouter.authz.AuthorizationResponse;
import org.onap.dmaap.datarouter.authz.Authorizer;

/** Authorizer for the provisioning API for Data Router R1.
 *
 * @author J. F. Lucas
 *
 */
public class ProvAuthorizer implements Authorizer {

    private final EELFLogger log;

    private static final String SUBJECT_HEADER = "X-DMAAP-DR-ON-BEHALF-OF";  // HTTP header carrying requester identity
    // HTTP header carrying requester identity  by group Rally : US708115

    public ProvAuthorizer() {
        this.log = EELFManager.getInstance().getLogger(this.getClass());
    }

    /**
     * Determine if the API request carried in the <code>request</code> parameter is permitted.
     *
     * @param request the HTTP request for which an authorization decision is needed
     * @return an object implementing the <code>AuthorizationResponse</code> interface.  This object includes the
     * permit/deny decision for the request and (after R1) supplemental information related to the response in the form
     * of advice and obligations.
     */
    @Override
    public AuthorizationResponse decide(HttpServletRequest request) {
        return this.decide(request, null);
    }

    /**
     * Determine if the API request carried in the <code>request</code> parameter,with additional attributes provided in
     * the <code>additionalAttrs</code> parameter, is permitted.   <code>additionalAttrs</code> isn't used in R1.
     *
     * @param request the HTTP request for which an authorization decision is needed
     * @param additionalAttrs additional attributes that the <code>Authorizer</code> can in making a decision
     * @return an object implementing the <code>AuthorizationResponse</code> interface.  This object includes the
     * permit/deny decision for the request and (after R1) supplemental information related to the response in the form
     * of advice and obligations.
     */
    @Override
    public AuthorizationResponse decide(HttpServletRequest request,
            Map<String, String> additionalAttrs) {
        log.trace("Entering decide()");
        // Extract interesting parts of the HTTP request
        String method = request.getMethod();
        AuthzResource resource = new AuthzResource(request.getRequestURI());
        String subject = (request.getHeader(SUBJECT_HEADER));

        log.trace("Method: " + method + " -- Type: " + resource.getType() + " -- Id: " + resource.getId()
                          + " -- Subject: " + subject);

        return new AuthRespImpl(true);
    }

}
