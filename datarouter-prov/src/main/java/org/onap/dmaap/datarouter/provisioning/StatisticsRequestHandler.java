package org.onap.dmaap.datarouter.provisioning;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.onap.dmaap.datarouter.provisioning.store.StatisticsStore;
import org.onap.dmaap.datarouter.provisioning.utils.ProvDbUtils;

import static org.onap.dmaap.datarouter.provisioning.BaseServlet.FEEDIDS;
import static org.onap.dmaap.datarouter.provisioning.StatisticsServlet.FEEDID;

public class StatisticsRequestHandler {
  private static final StatisticsStore statisticsStore = new StatisticsStore();

  private static final EELFLogger eventlogger = EELFManager.getInstance().getLogger("EventLog");
  public void handleRequest(HttpServletResponse resp, Map<String, String> map, String feedId, String groupId, String outputType, ServletOutputStream responseStream) {
    if (feedId == null && groupId == null) {
      try {
        responseStream.print("Invalid request, Feedid or Group ID is required.");
      } catch (IOException ioe) {
        eventlogger.error("PROV0171 StatisticsServlet.doGet: " + ioe.getMessage(), ioe);
      }
    }
    if (feedId != null && groupId == null) {
      map.put(FEEDIDS, feedId);
    }
    if (groupId != null && feedId == null) {
      StringBuilder groupid1;
      try {
        groupid1 = this.getFeedIdsByGroupId(Integer.parseInt(groupId));
        map.put(FEEDIDS, groupid1.toString());
      } catch (NumberFormatException e) {
        eventlogger.error("PROV0172 StatisticsServlet.doGet: " + e.getMessage(), e);
      }
    }
    if (groupId != null && feedId != null) {
      StringBuilder groupid1;
      try {
        groupid1 = this.getFeedIdsByGroupId(Integer.parseInt(groupId));
        groupid1.append(",");
        groupid1.append(feedId.replace("|", ","));
        map.put(FEEDIDS, groupid1.toString());
      } catch (NumberFormatException e) {
        eventlogger.error("PROV0173 StatisticsServlet.doGet: " + e.getMessage(), e);
      }
    }
    String feedids = "";
    if (map.get(FEEDIDS) != null) {
      feedids = map.get(FEEDIDS);
    }
    statisticsStore.getRecordsForSQL(feedids, outputType, responseStream, resp);
  }

  /**
   * getFeedIdsByGroupId - Getting FEEDID's by GROUP ID.
   *
   * @param groupIds Integer ref of Group
   */
  private StringBuilder getFeedIdsByGroupId(int groupIds) {
    StringBuilder feedIds = new StringBuilder();
    try (Connection conn = ProvDbUtils.getInstance().getConnection();
         PreparedStatement prepareStatement = conn.prepareStatement(
           " SELECT FEEDID from FEEDS  WHERE GROUPID = ?")) {
      prepareStatement.setInt(1, groupIds);
      try (ResultSet resultSet = prepareStatement.executeQuery()) {
        while (resultSet.next()) {
          feedIds.append(resultSet.getInt(FEEDID));
          feedIds.append(",");
        }
      }
      feedIds.deleteCharAt(feedIds.length() - 1);
      eventlogger.info("PROV0177 StatisticsServlet.getFeedIdsByGroupId: feedIds = " + feedIds.toString());
    } catch (SQLException e) {
      eventlogger.error("PROV0175 StatisticsServlet.getFeedIdsByGroupId: " + e.getMessage(), e);
    }
    return feedIds;
  }

}
