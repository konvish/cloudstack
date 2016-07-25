package org.apache.sirona.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.sirona.spi.SPI;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Validation;
import org.apache.sirona.status.ValidationFactory;
import org.apache.sirona.status.ValidationResult;
/**
 * Created by kong on 2016/1/24.
 */
public class NodeStatusReporter {
    public NodeStatusReporter() {
    }

    public synchronized NodeStatus computeStatus() {
        Validation[] validations = this.reload();
        ArrayList results = new ArrayList(validations.length);
        Validation[] arr$ = validations;
        int len$ = validations.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Validation v = arr$[i$];
            results.add(v.validate());
        }

        return new NodeStatus((ValidationResult[])results.toArray(new ValidationResult[results.size()]), new Date());
    }

    public synchronized Validation[] reload() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        LinkedList val = new LinkedList();
        Iterator i$ = SPI.INSTANCE.find(Validation.class, classLoader).iterator();

        while(i$.hasNext()) {
            Validation f = (Validation)i$.next();
            val.add(f);
        }

        i$ = SPI.INSTANCE.find(ValidationFactory.class, classLoader).iterator();

        while(i$.hasNext()) {
            ValidationFactory f1 = (ValidationFactory)i$.next();
            val.addAll(Arrays.asList(f1.validations()));
        }

        return (Validation[])val.toArray(new Validation[val.size()]);
    }
}

