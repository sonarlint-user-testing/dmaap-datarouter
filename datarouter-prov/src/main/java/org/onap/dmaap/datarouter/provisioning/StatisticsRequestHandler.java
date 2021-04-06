package org.onap.dmaap.datarouter.provisioning;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.onap.dmaap.datarouter.provisioning.store.StatisticsStore;

public class StatisticsRequestHandler {
  private static final StatisticsStore statisticsStore = new StatisticsStore();

  private static final EELFLogger eventlogger = EELFManager.getInstance().getLogger("EventLog");

  public void handleRequest(HttpServletResponse resp, String feedId, String outputType, ServletOutputStream responseStream) {
    if (feedId == null) {
      try {
        responseStream.print("Invalid request, Feedid or Group ID is required.");
        responseStream.flush();
      } catch (IOException ioe) {
        eventlogger.error("PROV0171 StatisticsServlet.doGet: " + ioe.getMessage(), ioe);
      }
      return;
    }
    statisticsStore.getRecordsForSQL(feedId, outputType, responseStream, resp);
  }

}
