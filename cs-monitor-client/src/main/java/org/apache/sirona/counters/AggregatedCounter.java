package org.apache.sirona.counters;

import java.util.Map;
import org.apache.sirona.counters.Counter;
/**
 * Created by kong on 2016/1/24.
 */
public interface AggregatedCounter {
    Map<String, ? extends Counter> aggregated();
}
