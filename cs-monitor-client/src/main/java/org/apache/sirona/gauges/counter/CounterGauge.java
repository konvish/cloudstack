package org.apache.sirona.gauges.counter;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.MetricData;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
/**
 * Created by kong on 2016/1/24.
 */
public class CounterGauge implements Gauge {
    protected final Counter counter;
    protected final MetricData metric;
    protected final Role role;

    protected CounterGauge(Counter counter) {
        this(counter, MetricData.Sum);
    }

    public CounterGauge(Counter counter, MetricData metric) {
        this.counter = counter;
        this.metric = metric;
        this.role = new Role("counter-" + counter.getKey().getRole().getName() + "-" + counter.getKey().getName() + "-" + metric.name(), Unit.UNARY);
    }

    public Role role() {
        return this.role;
    }

    public double value() {
        return this.metric.value(this.counter);
    }
}
