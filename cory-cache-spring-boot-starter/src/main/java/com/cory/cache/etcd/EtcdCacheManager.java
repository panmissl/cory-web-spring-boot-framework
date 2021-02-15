package com.cory.cache.etcd;

import mousio.client.retry.RetryOnce;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdErrorCode;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * cache结构：

 ROOT
 +---- CacheA
 +--------ID1: cache1
 +--------ID2: cache2
 +---- CacheB
 +--------ID3: cache3
 +--------ID4: cache4

 * Created by Cory on 2018/7/22.
 */
public class EtcdCacheManager extends AbstractTransactionSupportingCacheManager {

    public static final String CACHE_ROOT_KEY = "_etcd_spring_cache_root_/";

    private EtcdClient client;

    public EtcdCacheManager(List<String> servers) {
        assert CollectionUtils.isEmpty(servers) : "servers can't be empty!";

        List<URI> uriList = servers.stream().map(server -> URI.create(server)).collect(Collectors.toList());
        URI[] uris = uriList.toArray(new URI[uriList.size()]);
        client = new EtcdClient(uris);
        client.setRetryHandler(new RetryOnce(20));
        ensureRootDir();
    }

    private void ensureRootDir() {
        try {
            client.getDir(CACHE_ROOT_KEY).send().get().getNode().isDir();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (EtcdException e) {
            if (EtcdErrorCode.KeyNotFound == e.getErrorCode()) {
                createRootDir();
            } else {
                throw new RuntimeException(e);
            }
        } catch (EtcdAuthenticationException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void createRootDir() {
        try {
            client.putDir(CACHE_ROOT_KEY).send().get().getNode().isDir();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return this.loadAndInitRemoteCaches();
    }

    protected List<Cache> loadAndInitRemoteCaches() {
        ArrayList caches = new ArrayList();
        try {
            Set<String> cacheNames = this.loadRemoteCacheKeys();
            if(!CollectionUtils.isEmpty(cacheNames)) {
                Iterator cacheNameItr = cacheNames.iterator();
                while (cacheNameItr.hasNext()) {
                    String cacheName = (String) cacheNameItr.next();
                    if (null == super.getCache(cacheName)) {
                        caches.add(this.createCache(cacheName));
                    }
                }
            }
        } catch (Exception e) {
        }
        return caches;
    }

    protected Set<String> loadRemoteCacheKeys() {
        try {
            Set<String> keys = new HashSet<>();
            List<EtcdKeysResponse.EtcdNode> caches = client.getDir(CACHE_ROOT_KEY).send().get().getNode().getNodes();
            if (!CollectionUtils.isEmpty(caches)) {
                caches.forEach(cache -> {
                    List<EtcdKeysResponse.EtcdNode> concreteCaches = cache.getNodes();
                    if (!CollectionUtils.isEmpty(concreteCaches)) {
                        concreteCaches.forEach(concreteCache -> keys.add(concreteCache.getKey()));
                    }
                });
            }
            return keys;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = super.getCache(name);
        return cache == null ? this.createAndAddCache(name) : cache;
    }

    protected Cache createAndAddCache(String cacheName) {
        this.addCache(this.createCache(cacheName));
        return super.getCache(cacheName);
    }

    protected EtcdCache createCache(String cacheName) {
        return new EtcdCache(cacheName, this.client);
    }
}
