package org.apache.sirona.configuration.predicate;

/**
 * Created by kong on 2016/1/24.
 */
public interface Predicate {
    String prefix();

    boolean matches(String var1);

    void addConfiguration(String var1, boolean var2);
}