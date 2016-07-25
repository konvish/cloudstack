package org.apache.sirona.stopwatches;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit.Time;
import org.apache.sirona.stopwatches.StopWatch;
/**
 * Created by kong on 2016/1/24.
 */
public class CounterStopWatch implements StopWatch {
    protected final Counter counter;
    protected final long startedAt;
    protected final AtomicInteger concurrencyCounter;
    protected long stopedAt;
    protected boolean stoped;

    public CounterStopWatch(Counter counter) {
        this.counter = counter;
        this.startedAt = this.nanotime();
        this.concurrencyCounter = counter.currentConcurrency();
        int concurrency = this.concurrencyCounter.incrementAndGet();
        counter.updateConcurrency(concurrency);
    }

    protected long nanotime() {
        return System.nanoTime();
    }

    public long getElapsedTime() {
        return !this.stoped?this.nanotime() - this.startedAt:this.stopedAt - this.startedAt;
    }

    public StopWatch stop() {
        if(!this.stoped) {
            this.stopedAt = this.nanotime();
            this.stoped = true;
            this.doStop();
        }

        return this;
    }

    protected void doStop() {
        this.counter.add((double)this.getElapsedTime(), Time.NANOSECOND);
        this.concurrencyCounter.decrementAndGet();
    }

    public String toString() {
        StringBuilder stb = new StringBuilder();
        if(this.counter != null) {
            stb.append("Execution for ").append(this.counter.getKey().toString()).append(" ");
        }

        if(this.stoped) {
            stb.append("stoped after ").append(this.getElapsedTime()).append("ns");
        } else {
            stb.append("running for ").append(this.getElapsedTime()).append("ns");
        }

        return stb.toString();
    }
}
