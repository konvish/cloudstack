package org.apache.sirona.status;

import java.util.Date;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
/**
 * Created by kong on 2016/1/24.
 */
public class NodeStatus {
    private final ValidationResult[] results;
    private final Date date;
    private final Status status;

    public NodeStatus(ValidationResult[] results, Date date) {
        this.results = results;
        this.date = date;
        this.status = this.computeStatus();
    }

    public Date getDate() {
        return this.date == null?new Date(0L):this.date;
    }

    public ValidationResult[] getResults() {
        return this.results;
    }

    public Status getStatus() {
        return this.status;
    }

    protected Status computeStatus() {
        Status lowest = Status.OK;
        ValidationResult[] arr$ = this.results;
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            ValidationResult result = arr$[i$];
            if(Status.KO.equals(result.getStatus())) {
                return Status.KO;
            }

            if(Status.DEGRADED.equals(result.getStatus())) {
                lowest = Status.DEGRADED;
            }
        }

        return lowest;
    }
}