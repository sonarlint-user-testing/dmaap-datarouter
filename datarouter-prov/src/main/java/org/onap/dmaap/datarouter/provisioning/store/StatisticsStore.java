package org.onap.dmaap.datarouter.provisioning.store;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.onap.dmaap.datarouter.provisioning.utils.LOGJSONObject;
import org.onap.dmaap.datarouter.provisioning.utils.ProvDbUtils;

import static org.onap.dmaap.datarouter.provisioning.StatisticsServlet.FEEDID;

public class StatisticsStore {

  private static final EELFLogger eventlogger = EELFManager.getInstance().getLogger("EventLog");

  public void getRecordsForSQL(String feedids, String outputType, ServletOutputStream out,
                               HttpServletResponse resp) {
    try {
      try (Connection conn = ProvDbUtils.getInstance().getConnection();
           PreparedStatement ps = makePreparedStatement(feedids, conn);
           ResultSet rs = ps.executeQuery()) {
        if ("csv".equals(outputType)) {
          resp.setContentType("application/octet-stream");
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
          resp.setHeader("Content-Disposition",
            "attachment; filename=\"result:" + LocalDateTime.now().format(formatter) + ".csv\"");
          eventlogger.info("Generating CSV file from Statistics resultset");
          rsToCSV(rs, out);
        } else {
          eventlogger.info("Generating JSON for Statistics resultset");
          this.rsToJson(rs, out);
        }
      } catch (SQLException e) {
        eventlogger.error("SQLException:" + e);
      }
    } catch (IOException e) {
      eventlogger.error("IOException - Generating JSON/CSV:" + e);
    } catch (JSONException e) {
      eventlogger.error("JSONException - executing SQL query:" + e);
    } catch (ParseException e) {
      eventlogger.error("ParseException - executing SQL query:" + e);
    }
  }

  private PreparedStatement makePreparedStatement(String feedids, Connection conn) throws SQLException, ParseException {
    String sql;
    eventlogger.info("Generating sql query to get Statistics resultset. ");
    sql =  "SELECT * FROM LOG_RECORDS WHERE id in(" + feedids + ")";
    eventlogger.debug("SQL Query for Statistics resultset. " + sql);
    return conn.prepareStatement(sql);
  }


  /**
   * rsToJson - Converting RS to JSON object.
   *
   * @param out ServletOutputStream
   * @param rs as ResultSet
   * @throws IOException input/output exception
   * @throws SQLException SQL exception
   */
  private void rsToCSV(ResultSet rs, ServletOutputStream out) throws IOException, SQLException {
    String header = "FEEDNAME,FEEDID,FILES_PUBLISHED,PUBLISH_LENGTH, FILES_DELIVERED, "
      + "DELIVERED_LENGTH, SUBSCRIBER_URL, SUBID, PUBLISH_TIME,DELIVERY_TIME, AverageDelay\n";
    out.write(header.getBytes());

    while (rs.next()) {
      String line = rs.getString("FEEDNAME")
        + ","
        + rs.getString(FEEDID)
        + ","
        + rs.getString("FILES_PUBLISHED")
        + ","
        + rs.getString("PUBLISH_LENGTH")
        + ","
        + rs.getString("FILES_DELIVERED")
        + ","
        + rs.getString("DELIVERED_LENGTH")
        + ","
        + rs.getString("SUBSCRIBER_URL")
        + ","
        + rs.getString("SUBID")
        + ","
        + rs.getString("PUBLISH_TIME")
        + ","
        + rs.getString("DELIVERY_TIME")
        + ","
        + rs.getString("AverageDelay")
        + ","
        + "\n";
      out.write(line.getBytes());
      out.flush();
    }
  }

  /**
   * rsToJson - Converting RS to JSON object.
   *
   * @param out ServletOutputStream
   * @param rs as ResultSet
   * @throws IOException input/output exception
   * @throws SQLException SQL exception
   */
  private void rsToJson(ResultSet rs, ServletOutputStream out) throws IOException, SQLException {
    String[] fields = {"FEEDNAME", FEEDID, "FILES_PUBLISHED", "PUBLISH_LENGTH", "FILES_DELIVERED",
      "DELIVERED_LENGTH", "SUBSCRIBER_URL", "SUBID", "PUBLISH_TIME", "DELIVERY_TIME",
      "AverageDelay"};
    StringBuilder line = new StringBuilder();
    line.append("[\n");
    while (rs.next()) {
      LOGJSONObject j2 = new LOGJSONObject();
      for (String key : fields) {
        Object val = rs.getString(key);
        if (val != null) {
          j2.put(key.toLowerCase(), val);
        } else {
          j2.put(key.toLowerCase(), "");
        }
      }
      line.append(j2.toString());
      line.append(",\n");
    }
    line.append("]");
    out.print(line.toString());
  }
}
