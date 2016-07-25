package org.apache.sirona.configuration.predicate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;
import org.apache.sirona.configuration.predicate.Predicate;
import org.apache.sirona.spi.SPI;
/**
 * Created by kong on 2016/1/24.
 */
public final class PredicateEvaluator {
    private static final String NOT = "!";
    private static final char SEPARATOR = ':';
    private final Map<String, Predicate> predicates = new HashMap();
    private final boolean truePredicate;

    public PredicateEvaluator(String configuration, String sep) {
        if(configuration != null && !configuration.isEmpty()) {
            PredicateEvaluator.PrefixPredicate prefixPredicate = new PredicateEvaluator.PrefixPredicate();
            PredicateEvaluator.SuffixPredicate suffixPredicate = new PredicateEvaluator.SuffixPredicate();
            PredicateEvaluator.RegexPredicate regexPredicate = new PredicateEvaluator.RegexPredicate();
            PredicateEvaluator.ContainersPredicate containersPredicate = new PredicateEvaluator.ContainersPredicate();
            this.predicates.put(prefixPredicate.prefix(), prefixPredicate);
            this.predicates.put(suffixPredicate.prefix(), suffixPredicate);
            this.predicates.put(regexPredicate.prefix(), regexPredicate);
            this.predicates.put(containersPredicate.prefix(), containersPredicate);
            this.predicates.put(PredicateEvaluator.TruePredicate.INSTANCE.prefix(), PredicateEvaluator.TruePredicate.INSTANCE);
            Iterator segments = SPI.INSTANCE.find(Predicate.class, PredicateEvaluator.class.getClassLoader()).iterator();

            while(segments.hasNext()) {
                Predicate arr$ = (Predicate)segments.next();
                this.predicates.put(arr$.prefix(), arr$);
            }

            String[] var17 = configuration.split(sep);
            String[] var18 = var17;
            int len$ = var17.length;
            int i$ = 0;

            while(true) {
                if(i$ >= len$) {
                    this.truePredicate = false;
                    break;
                }

                String segment = var18[i$];
                String trim = segment.trim();
                int separator = trim.indexOf(58);
                if(separator <= 0 || trim.length() <= separator) {
                    throw new IllegalArgumentException("Need to specify a prefix, available are:" + this.predicates.keySet());
                }

                String prefix = trim.substring(0, separator);
                Predicate predicate = (Predicate)this.predicates.get(prefix);
                if(predicate == null) {
                    throw new IllegalArgumentException("Can\'t find prefix \'" + prefix + "\'");
                }

                if(predicate == PredicateEvaluator.TruePredicate.INSTANCE) {
                    this.truePredicate = true;
                    this.predicates.clear();
                    return;
                }

                String value = trim.substring(separator + 1);
                if(!value.startsWith("!")) {
                    predicate.addConfiguration(value, true);
                } else {
                    predicate.addConfiguration(value.substring(1), false);
                }

                ++i$;
            }
        } else {
            this.truePredicate = false;
        }

        this.predicates.remove(PredicateEvaluator.TruePredicate.INSTANCE.prefix());
    }

    public boolean matches(String value) {
        if(this.truePredicate) {
            return true;
        } else {
            Iterator i$ = this.predicates.values().iterator();

            Predicate predicate;
            do {
                if(!i$.hasNext()) {
                    return false;
                }

                predicate = (Predicate)i$.next();
            } while(!predicate.matches(value));

            return true;
        }
    }

    private static class RegexPredicate implements Predicate {
        private final Map<Pattern, Boolean> patterns;

        private RegexPredicate() {
            this.patterns = new HashMap();
        }

        public String prefix() {
            return "regex";
        }

        public boolean matches(String value) {
            Iterator i$ = this.patterns.entrySet().iterator();

            Entry p;
            do {
                if(!i$.hasNext()) {
                    return false;
                }

                p = (Entry)i$.next();
            } while(!((Pattern)p.getKey()).matcher(value).matches());

            return ((Boolean)p.getValue()).booleanValue();
        }

        public void addConfiguration(String value, boolean negative) {
            this.patterns.put(Pattern.compile(value), Boolean.valueOf(negative));
        }
    }

    private static class PrefixPredicate implements Predicate {
        private final Map<String, Boolean> prefixes;

        private PrefixPredicate() {
            this.prefixes = new HashMap();
        }

        public String prefix() {
            return "prefix";
        }

        public boolean matches(String value) {
            Iterator i$ = this.prefixes.entrySet().iterator();

            Entry p;
            do {
                if(!i$.hasNext()) {
                    return false;
                }

                p = (Entry)i$.next();
            } while(!value.startsWith((String)p.getKey()));

            return ((Boolean)p.getValue()).booleanValue();
        }

        public void addConfiguration(String value, boolean negative) {
            this.prefixes.put(value, Boolean.valueOf(negative));
        }
    }

    private static class SuffixPredicate implements Predicate {
        private final Map<String, Boolean> suffixes;

        private SuffixPredicate() {
            this.suffixes = new HashMap();
        }

        public String prefix() {
            return "suffix";
        }

        public boolean matches(String value) {
            Iterator i$ = this.suffixes.entrySet().iterator();

            Entry p;
            do {
                if(!i$.hasNext()) {
                    return false;
                }

                p = (Entry)i$.next();
            } while(!value.endsWith((String)p.getKey()));

            return ((Boolean)p.getValue()).booleanValue();
        }

        public void addConfiguration(String value, boolean negative) {
            this.suffixes.put(value, Boolean.valueOf(negative));
        }
    }

    private static class TruePredicate implements Predicate {
        private static final PredicateEvaluator.TruePredicate INSTANCE = new PredicateEvaluator.TruePredicate();

        private TruePredicate() {
        }

        public String prefix() {
            return "true";
        }

        public boolean matches(String value) {
            return true;
        }

        public void addConfiguration(String value, boolean negative) {
        }
    }

    private static class ContainersPredicate implements Predicate {
        private final Collection<String> containers;

        private ContainersPredicate() {
            this.containers = new CopyOnWriteArraySet();
        }

        public String prefix() {
            return "container";
        }

        public boolean matches(String value) {
            Iterator i$ = this.containers.iterator();

            while(i$.hasNext()) {
                String container = (String)i$.next();
                String length;
                String sub;
                if(!"tomee".equalsIgnoreCase(container) && !"openejb".equalsIgnoreCase(container)) {
                    if("tomcat".equalsIgnoreCase(container)) {
                        if(value.startsWith("org.")) {
                            length = value.substring("org.".length());
                            if(length.startsWith("apache.")) {
                                sub = value.substring("org.apache.".length());
                                if(isTomcat(sub)) {
                                    return true;
                                }
                            } else if(length.startsWith("eclipse.jdt")) {
                                return true;
                            }
                        }
                    } else if("jvm".equalsIgnoreCase(container)) {
                        if(value.startsWith("java") || value.startsWith("sun") || value.startsWith("com.sun") || value.startsWith("jdk.")) {
                            return true;
                        }

                        if(value.startsWith("org.")) {
                            length = value.substring("org.".length());
                            if(length.startsWith("omg") || length.startsWith("xml.sax.") || length.startsWith("ietf") || length.startsWith("jcp") || length.startsWith("apache.xerces")) {
                                return true;
                            }
                        }

                        int length1 = "org.apache.".length();
                        if(value.length() >= length1) {
                            sub = value.substring("org.apache.".length());
                            if(sub.startsWith("xerces")) {
                                return true;
                            }
                        }
                    }
                } else if(value.startsWith("org.")) {
                    length = value.substring("org.".length());
                    if(length.startsWith("apache.")) {
                        sub = length.substring("apache.".length());
                        if(isTomcat(sub) || sub.startsWith("tomee") || sub.startsWith("openejb") || sub.startsWith("xbean") || sub.startsWith("bval") || sub.startsWith("openjpa") || sub.startsWith("geronimo") || sub.startsWith("webbeans") || sub.startsWith("myfaces") || sub.startsWith("cxf") || sub.startsWith("neethi") || sub.startsWith("activemq") || sub.startsWith("commons")) {
                            return true;
                        }
                    } else if(length.startsWith("slf4j.") || length.startsWith("metatype") || length.startsWith("hsqldb") || length.startsWith("eclipse.jdt")) {
                        return true;
                    }
                } else if(value.startsWith("serp")) {
                    return true;
                }
            }

            return false;
        }

        private static boolean isTomcat(String sub) {
            return sub.startsWith("juli.") || sub.startsWith("catalina.") || sub.startsWith("tomcat.") || sub.startsWith("jasper.") || sub.startsWith("coyote.") || sub.startsWith("naming.") || sub.startsWith("el.");
        }

        public void addConfiguration(String value, boolean negative) {
            this.containers.add(value);
        }
    }
}
