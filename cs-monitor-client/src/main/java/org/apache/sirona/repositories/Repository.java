package org.apache.sirona.repositories;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.stopwatches.StopWatch;
/**
 * Created by kong on 2016/1/24.
 */
public interface Repository {
    Repository INSTANCE = (Repository)IoCs.findOrCreateInstance(Repository.class);

    Counter getCounter(Key var1);

    Collection<Counter> counters();

    void clearCounters();

    void reset();

    StopWatch start(Counter var1);

    void addGauge(Gauge var1);

    void stopGauge(Gauge var1);

    SortedMap<Long, Double> getGaugeValues(long var1, long var3, Role var5);

    Collection<Role> gauges();

    Role findGaugeRole(String var1);

    Map<String, NodeStatus> statuses();
}
