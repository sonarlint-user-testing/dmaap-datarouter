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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Internal representation of an authorization resource (the entity to which access is being requested).  Consists
 * of a type and an identifier.   The constructor takes the request URI from an HTTP request and checks it against
 * patterns for the the different resource types.  In DR R1, there are four resource types:
 * <li>the feeds collection resource, the target of POST requests to create a new feed and GET requests to list
 * the existing feeds.  This is the root resource for the DR provisioning system, and it has no explicit id.
 * </li>
 * <li>a feed resource, the target of GET, PUT, and DELETE requests used to manage an existing feed.  Each feed
 * has a unique feed ID.
 * </li>
 * <li>a subscription collection resource, the target of POST requests to create a new subscription and GET requests
 * to list the subscriptions for a feed.  Each feed has a subscription collection, and the ID associated with a
 * subscription collection is the ID of the feed.
 * </li>
 * <li>a subscription resource, the target of GET, PUT, and DELETE requests used to manage an existing subscription.
 * Each subscription has a unique subscription ID.
 * </li>
 *
 * @author J. F. Lucas
 *
 */
public class AuthzResource {
    private ResourceType type = null;
    private String id = "";

    /* Construct an AuthzResource by matching a request URI against the various patterns */
    AuthzResource(String requestUri) {
        if (requestUri != null) {
            for (ResourceType t : ResourceType.values()) {
                Matcher match = t.getPattern().matcher(requestUri);
                if (match.find(0)) {
                    this.type = t;
                    if (match.group("id") != null) {
                        this.id = match.group("id");
                    }
                    break;
                }
            }
        }
    }

    public ResourceType getType() {
        return this.type;
    }

    public String getId() {
        return this.id;
    }

    /* Enumeration that helps turn a request URI into something more useful for
     * authorization purposes by given a type name and a pattern for determining if the URI
     * represents that resource type.
     * Highly dependent on the URL scheme, could be parameterized.
     */
    public enum ResourceType {
        FEEDS_COLLECTION("((://[^/]+/)|(^/))(?<id>)$"),
        SUBS_COLLECTION("((://[^/]+/)|(^/{0,1}))subscribe/(?<id>[^/]+)$"),
        FEED("((://[^/]+/)|(^/{0,1}))feed/(?<id>[^/]+)$"),
        SUB("((://[^/]+/)|(^/{0,1}))subs/(?<id>[^/]+)$");

        private Pattern uriPattern;

        ResourceType(String patternString) {
            this.uriPattern = Pattern.compile(patternString);
        }

        Pattern getPattern() {
            return this.uriPattern;
        }
    }
}
