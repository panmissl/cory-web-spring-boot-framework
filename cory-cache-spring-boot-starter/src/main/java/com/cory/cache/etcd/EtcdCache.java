package com.cory.cache.etcd;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdErrorCode;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static com.cory.cache.etcd.EtcdCacheManager.CACHE_ROOT_KEY;

/**
 * Created by Cory on 2018/7/22.
 */
public class EtcdCache implements Cache {

    private String cacheName;
    private EtcdClient client;

    public EtcdCache(String cacheName, EtcdClient client) {
        this.cacheName = cacheName;
        this.client = client;
        ensureCacheDir();
    }

    private void ensureCacheDir() {
        String key = EtcdCacheHelper.buildCacheKey(cacheName);
        try {
            client.getDir(key).send().get().getNode().isDir();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (EtcdException e) {
            if (EtcdErrorCode.KeyNotFound == e.getErrorCode()) {
                createDir(key);
            } else {
                throw new RuntimeException(e);
            }
        } catch (EtcdAuthenticationException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDir(String key) {
        try {
            client.putDir(key).send().get().getNode().isDir();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public Object getNativeCache() {
        return client;
    }

    @Override
    public ValueWrapper get(Object key) {
        try {
            String data = client.get(EtcdCacheHelper.buildCacheKey(cacheName, key.toString())).send().get().getNode().getValue();
            return toWrapper(EtcdCacheHelper.parseValue(data));
        } catch (EtcdException e) {
            if (EtcdErrorCode.KeyNotFound == e.getErrorCode()) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T get(Object key, Class<T> clazz) {
        try {
            String data = client.get(EtcdCacheHelper.buildCacheKey(cacheName, key.toString())).send().get().getNode().getValue();
            return (T) EtcdCacheHelper.parseValue(data);
        } catch (EtcdException e) {
            if (EtcdErrorCode.KeyNotFound == e.getErrorCode()) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper storeValue = this.get(key);
        if(storeValue != null) {
            return (T) storeValue.get();
        } else {
            synchronized(key) {
                storeValue = this.get(key);
                if(storeValue != null) {
                    return (T) storeValue.get();
                } else {
                    Object value;
                    try {
                        value = valueLoader.call();
                    } catch (Throwable var8) {
                        throw new ValueRetrievalException(key, valueLoader, var8);
                    }

                    this.put(key, value);
                    return (T) value;
                }
            }
        }
    }

    @Override
    public void put(Object key, Object value) {
        try {
            client.put(EtcdCacheHelper.buildCacheKey(cacheName, key.toString()), EtcdCacheHelper.buildValue(value)).send();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        try {
            String data = client.get(EtcdCacheHelper.buildCacheKey(cacheName, key.toString())).send().get().getNode().getValue();
            return toWrapper(EtcdCacheHelper.parseValue(data));
        } catch (EtcdException e) {
            if (EtcdErrorCode.KeyNotFound == e.getErrorCode()) {
                put(key, value);
                return toWrapper(value);
            } else {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void evict(Object key) {
        try {
            client.delete(EtcdCacheHelper.buildCacheKey(cacheName, key.toString())).send();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        try {
            List<EtcdKeysResponse.EtcdNode> caches = client.getDir(CACHE_ROOT_KEY).send().get().getNode().getNodes();
            if (CollectionUtils.isEmpty(caches)) {
                return;
            }
            caches.forEach(cache -> {
                List<EtcdKeysResponse.EtcdNode> concreteCaches = cache.getNodes();
                if (!CollectionUtils.isEmpty(concreteCaches)) {
                    concreteCaches.forEach(concreteCache -> {
                        try {
                            client.delete(EtcdCacheHelper.buildCacheKey(cacheName, concreteCache.getKey())).send();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ValueWrapper toWrapper(Object value) {
        return value != null ? new SimpleValueWrapper(value) : null;
    }
}
