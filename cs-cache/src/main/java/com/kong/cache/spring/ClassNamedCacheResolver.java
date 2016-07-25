package com.kong.cache.spring;

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

import java.util.Collection;
import java.util.Set;
/**
 * Created by kong on 2016/1/22.
 */
public class ClassNamedCacheResolver extends AbstractCacheResolver {
    public ClassNamedCacheResolver() {
    }

    public ClassNamedCacheResolver(CacheManager cacheManager) {
        super(cacheManager);
    }

    protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        Set cacheNames = context.getOperation().getCacheNames();
        if(cacheNames.size() > 0 && "allunused".equals(cacheNames.iterator().next()) && context.getTarget() != null && context.getTarget().toString().contains("@")) {
            cacheNames.clear();
            String className = context.getTarget().toString().split("\\@")[0];
            String[] pkgName = className.split("\\.");
            StringBuilder simpleName = new StringBuilder(20);
            int i = 0;

            for(int size = pkgName.length - 1; i < size; ++i) {
                simpleName.append(pkgName[i].charAt(0)).append(".");
            }

            simpleName.append(pkgName[pkgName.length - 1]);
            cacheNames.add(simpleName.toString());
        }

        return cacheNames;
    }
}