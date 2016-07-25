package org.apache.sirona.store.gauge;

import org.apache.sirona.Role;
/**
 * Created by kong on 2016/1/24.
 */
public class GaugeValuesRequest {
    private long start;
    private long end;
    private Role role;

    public GaugeValuesRequest() {
    }

    public GaugeValuesRequest(long start, long end, Role role) {
        this.start = start;
        this.end = end;
        this.role = role;
    }

    public long getStart() {
        return this.start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return this.end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
