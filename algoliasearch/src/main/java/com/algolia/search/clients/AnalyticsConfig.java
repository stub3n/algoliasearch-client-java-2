package com.algolia.search.clients;

import com.algolia.search.models.CallType;
import com.algolia.search.transport.StatefulHost;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class AnalyticsConfig extends AlgoliaConfig {

  public AnalyticsConfig(String applicationID, String apiKey) {
    super(applicationID, apiKey);

    List<StatefulHost> hosts =
        Collections.singletonList(
            new StatefulHost(
                "analytics.algolia.com",
                true,
                LocalDate.now(ZoneOffset.UTC),
                EnumSet.of(CallType.READ, CallType.WRITE)));

    this.setDefaultHosts(hosts);
  }
}