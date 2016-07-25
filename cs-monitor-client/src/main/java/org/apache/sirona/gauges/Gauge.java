package org.apache.sirona.gauges;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.sirona.Role;
import org.apache.sirona.gauges.GaugeFactory;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.spi.SPI;
/**
 * Created by kong on 2016/1/24.
 */
public interface Gauge {
    Role role();

    double value();

    public static class LoaderHelper {
        private LinkedList<Gauge> gauges;

        public LoaderHelper(boolean excludeParent, Collection<? extends Gauge> manualGauges, String... includedPrefixes) {
            this.gauges = new LinkedList();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Iterator i$ = manualGauges.iterator();

            Gauge gf;
            while(i$.hasNext()) {
                gf = (Gauge)i$.next();
                this.addGauge(gf);
            }

            i$ = SPI.INSTANCE.find(Gauge.class, classLoader).iterator();

            while(i$.hasNext()) {
                gf = (Gauge)i$.next();
                this.addGaugeIfNecessary(classLoader, gf, excludeParent, includedPrefixes);
            }

            i$ = SPI.INSTANCE.find(GaugeFactory.class, classLoader).iterator();

            while(true) {
                Gauge[] list;
                do {
                    if(!i$.hasNext()) {
                        return;
                    }

                    GaugeFactory var12 = (GaugeFactory)i$.next();
                    list = var12.gauges();
                } while(list == null);

                Gauge[] arr$ = list;
                int len$ = list.length;

                for(int i$1 = 0; i$1 < len$; ++i$1) {
                    Gauge g = arr$[i$1];
                    this.addGaugeIfNecessary(classLoader, g, excludeParent, includedPrefixes);
                }
            }
        }

        public LoaderHelper(boolean excludeParent, String... includedPrefixes) {
            this(excludeParent, Collections.emptyList(), includedPrefixes);
        }

        private void addGaugeIfNecessary(ClassLoader classLoader, Gauge g, boolean excludeParent, String... prefixes) {
            Class gaugeClass = g.getClass();
            if(!excludeParent || gaugeClass.getClassLoader() == classLoader) {
                if(prefixes != null && prefixes.length > 0) {
                    boolean found = false;
                    String[] arr$ = prefixes;
                    int len$ = prefixes.length;

                    for(int i$ = 0; i$ < len$; ++i$) {
                        String p = arr$[i$];
                        if(gaugeClass.getName().startsWith(p.trim())) {
                            found = true;
                            break;
                        }
                    }

                    if(!found) {
                        return;
                    }
                }

                this.addGauge(g);
            }

        }

        private void addGauge(Gauge g) {
            Repository.INSTANCE.addGauge(g);
            this.gauges.add(g);
        }

        public void destroy() {
            Iterator i$ = this.gauges.iterator();

            while(i$.hasNext()) {
                Gauge gauge = (Gauge)i$.next();
                Repository.INSTANCE.stopGauge(gauge);
            }

            this.gauges.clear();
        }
    }
}