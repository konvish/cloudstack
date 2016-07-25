package org.apache.sirona.gauges;

import org.apache.sirona.gauges.Gauge;
/**
 * Created by kong on 2016/1/24.
 */
public interface GaugeFactory {
    Gauge[] gauges();
}