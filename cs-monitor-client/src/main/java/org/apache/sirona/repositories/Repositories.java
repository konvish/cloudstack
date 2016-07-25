package org.apache.sirona.repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.repositories.Repository;
/**
 * Created by kong on 2016/1/24.
 */
public class Repositories {
    public static Collection<Role> findByPrefixAndUnit(String prefix, Unit unit) {
        LinkedList roles = new LinkedList();
        Iterator i$ = Repository.INSTANCE.gauges().iterator();

        while(i$.hasNext()) {
            Role role = (Role)i$.next();
            if(role.getName().startsWith(prefix) && unit.equals(role.getUnit())) {
                roles.add(role);
            }
        }

        return roles;
    }

    public static Collection<Role> findBySuffixAndUnit(String suffix, Unit unit) {
        LinkedList roles = new LinkedList();
        Iterator i$ = Repository.INSTANCE.gauges().iterator();

        while(i$.hasNext()) {
            Role role = (Role)i$.next();
            if(role.getName().endsWith(suffix) && unit.equals(role.getUnit())) {
                roles.add(role);
            }
        }

        return roles;
    }

    public static Collection<String> names(Collection<Role> membersGauges) {
        ArrayList names = new ArrayList(membersGauges.size());
        Iterator i$ = membersGauges.iterator();

        while(i$.hasNext()) {
            Role role = (Role)i$.next();
            names.add(role.getName());
        }

        return names;
    }

    private Repositories() {
    }
}