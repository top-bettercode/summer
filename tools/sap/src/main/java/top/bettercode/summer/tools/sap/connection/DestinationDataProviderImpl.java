package top.bettercode.summer.tools.sap.connection;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.util.StringUtils;

public class DestinationDataProviderImpl implements DestinationDataProvider {

  private final Map<String, Properties> provider = new HashMap<>();

  public void addDestinationProperties(String destName, Properties props) {
    provider.put(destName, props);
  }

  @Override
  public Properties getDestinationProperties(String destName) {
    if (!StringUtils.hasText(destName)) {
      throw new NullPointerException("Destinantion name is empty.");
    }

    if (provider.size() == 0) {
      throw new IllegalStateException("Data provider is empty.");
    }

    return provider.get(destName);
  }

  @Override
  public void setDestinationDataEventListener(DestinationDataEventListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean supportsEvents() {
    return false;
  }
}