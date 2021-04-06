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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.onap.dmaap.datarouter.provisioning.beans.EventLogRecord;
import org.onap.dmaap.datarouter.provisioning.utils.LOGJSONObject;

import static org.onap.dmaap.datarouter.provisioning.utils.HttpServletUtils.sendResponseError;


/**
 * This Servlet handles requests to the &lt;Statistics API&gt; and  &lt;Statistics consilidated
 * resultset&gt;.
 *
 * @author Manish Singh
 * @version $Id: StatisticsServlet.java,v 1.11 2016/08/10 17:27:02 Manish Exp $
 */
@SuppressWarnings("serial")


public class StatisticsServlet extends BaseServlet {

    private static final long TWENTYFOUR_HOURS = (24 * 60 * 60 * 1000L);
    private static final String FMT1 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String FMT2 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String FEEDID = "FEEDID";

    //sql Strings
    private static final String SQL_SELECT_NAME = "SELECT (SELECT NAME FROM FEEDS AS f WHERE f.FEEDID in(";
    private static final String SQL_FEED_ID = ") and f.FEEDID=e.FEEDID) AS FEEDNAME, e.FEEDID as FEEDID, ";
    private static final String SQL_SELECT_COUNT = "(SELECT COUNT(*) FROM LOG_RECORDS AS c WHERE c.FEEDID in(";
    private static final String SQL_TYPE_PUB = ") and c.FEEDID=e.FEEDID AND c.TYPE='PUB') AS FILES_PUBLISHED,";
    private static final String SQL_SELECT_SUM = "(SELECT SUM(content_length) FROM LOG_RECORDS AS c WHERE c.FEEDID in(";
    private static final String SQL_PUBLISH_LENGTH = ") and c.FEEDID=e.FEEDID AND c.TYPE='PUB') AS PUBLISH_LENGTH, COUNT(e.EVENT_TIME) as FILES_DELIVERED,";
    private static final String SQL_SUBSCRIBER_URL = " sum(m.content_length) as DELIVERED_LENGTH, SUBSTRING_INDEX(e.REQURI,'/',+3) as SUBSCRIBER_URL,";
    private static final String SQL_SUB_ID = " e.DELIVERY_SUBID as SUBID, ";
    private static final String SQL_DELIVERY_TIME = " e.EVENT_TIME AS PUBLISH_TIME, m.EVENT_TIME AS DELIVERY_TIME, ";
    private static final String SQL_AVERAGE_DELAY = " AVG(e.EVENT_TIME - m.EVENT_TIME)/1000 as AverageDelay FROM LOG_RECORDS";
    private static final String SQL_JOIN_RECORDS = " e JOIN LOG_RECORDS m ON m.PUBLISH_ID = e.PUBLISH_ID AND e.FEEDID IN (";
    private static final String SQL_STATUS_204 = " AND m.STATUS=204 AND e.RESULT=204 ";
    private static final String SQL_GROUP_SUB_ID = " group by SUBID";
    private static final StatisticsRequestHandler requestHandler = new StatisticsRequestHandler();

    /**
     * DELETE a logging URL -- not supported.
     */
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String message = "DELETE not allowed for the logURL.";
        EventLogRecord elr = new EventLogRecord(req);
        elr.setMessage(message);
        elr.setResult(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        eventlogger.error(elr.toString());
        sendResponseError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, message, eventlogger);
    }

    /**
     * GET a Statistics URL -- retrieve Statistics data for a feed or subscription. See the
     * <b>Statistics API</b> document for details on how this     method should be invoked.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> map = buildMapFromRequest(req);
        if (map.get("err") != null) {
            sendResponseError(resp, HttpServletResponse.SC_BAD_REQUEST,
                "Invalid arguments: " + map.get("err"), eventlogger);
            return;
        }
        // check Accept: header??
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(LOGLIST_CONTENT_TYPE);
        String feedId = req.getParameter(FEEDID);
        String groupId = req.getParameter(GROUPID);
        String outputType = "json";
        if (req.getParameter(OUTPUT_TYPE) != null) {
            outputType = req.getParameter(OUTPUT_TYPE);
        }
        ServletOutputStream responseStream = resp.getOutputStream();
        requestHandler.handleRequest(resp, map, feedId, groupId, outputType, responseStream);
    }


    /**
     * PUT a Statistics URL -- not supported.
     */
    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) {
        String message = "PUT not allowed for the StatisticsURL.";
        EventLogRecord elr = new EventLogRecord(req);
        elr.setMessage(message);
        elr.setResult(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        eventlogger.error(elr.toString());
        sendResponseError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, message, eventlogger);
    }

    /**
     * POST a Statistics URL -- not supported.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String message = "POST not allowed for the StatisticsURL.";
        EventLogRecord elr = new EventLogRecord(req);
        elr.setMessage(message);
        elr.setResult(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        eventlogger.error(elr.toString());
        sendResponseError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, message, eventlogger);
    }

    private Map<String, String> buildMapFromRequest(HttpServletRequest req) {
        Map<String, String> map = new HashMap<>();
        String str = req.getParameter("type");
        if (str != null) {
            if ("pub".equals(str) || "del".equals(str) || "exp".equals(str)) {
                map.put("type", str);
            } else {
                map.put("err", "bad type");
                return map;
            }
        } else {
            map.put("type", "all");
        }
        map.put("publishSQL", "");
        map.put("statusSQL", "");
        map.put("resultSQL", "");
        map.put(REASON_SQL, "");

        str = req.getParameter("publishId");
        if (str != null) {
            if (str.indexOf("'") >= 0) {
                map.put("err", "bad publishId");
                return map;
            }
            map.put("publishSQL", " AND PUBLISH_ID = '" + str + "'");
        }

        str = req.getParameter("statusCode");
        if (str != null) {
            String sql = null;
            switch (str) {
                case "success":
                    sql = " AND STATUS >= 200 AND STATUS < 300";
                    break;
                case "redirect":
                    sql = " AND STATUS >= 300 AND STATUS < 400";
                    break;
                case "failure":
                    sql = " AND STATUS >= 400";
                    break;
                default:
                    try {
                        int statusCode = Integer.parseInt(str);
                        if ((statusCode >= 100 && statusCode < 600) || (statusCode == -1)) {
                            sql = " AND STATUS = " + statusCode;
                        }
                    } catch (NumberFormatException e) {
                        eventlogger.error("Failed to parse input", e);
                    }
                    break;
            }
            if (sql == null) {
                map.put("err", "bad statusCode");
                return map;
            }
            map.put("statusSQL", sql);
            map.put("resultSQL", sql.replaceAll("STATUS", "RESULT"));
        }

        str = req.getParameter("expiryReason");
        if (str != null) {
            map.put("type", "exp");
            switch (str) {
                case "notRetryable":
                    map.put(REASON_SQL, " AND REASON = 'notRetryable'");
                    break;
                case "retriesExhausted":
                    map.put(REASON_SQL, " AND REASON = 'retriesExhausted'");
                    break;
                case "diskFull":
                    map.put(REASON_SQL, " AND REASON = 'diskFull'");
                    break;
                case "other":
                    map.put(REASON_SQL, " AND REASON = 'other'");
                    break;
                default:
                    map.put("err", "bad expiryReason");
                    return map;
            }
        }

        long stime = getTimeFromParam(req.getParameter("start"));
        if (stime < 0) {
            map.put("err", "bad start");
            return map;
        }
        long etime = getTimeFromParam(req.getParameter("end"));
        if (etime < 0) {
            map.put("err", "bad end");
            return map;
        }
        if (stime == 0 && etime == 0) {
            etime = System.currentTimeMillis();
            stime = etime - TWENTYFOUR_HOURS;
        } else if (stime == 0) {
            stime = etime - TWENTYFOUR_HOURS;
        } else if (etime == 0) {
            etime = stime + TWENTYFOUR_HOURS;
        }
        map.put("timeSQL", String.format(" AND EVENT_TIME >= %d AND EVENT_TIME <= %d", stime, etime));
        return map;
    }

    private long getTimeFromParam(final String str) {
        if (str == null) {
            return 0;
        }
        try {
            // First, look for an RFC 3339 date
            String fmt = (str.indexOf('.') > 0) ? FMT2 : FMT1;
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            Date date = sdf.parse(str);
            return date.getTime();
        } catch (ParseException e) {
            intlogger.error("Exception in getting Time :- " + e.getMessage(), e);
        }
        try {
            // Also allow a long (in ms); useful for testing
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            intlogger.error("Exception in getting Time :- " + e.getMessage(), e);
        }
        intlogger.info("Error parsing time=" + str);
        return -1;
    }
}

