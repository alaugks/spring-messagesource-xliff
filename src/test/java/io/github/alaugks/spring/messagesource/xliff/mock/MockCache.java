package io.github.alaugks.spring.messagesource.xliff.mock;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class MockCache implements Cache {

    Map<Object, Object> arrayCache = new HashMap<>();

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Object getNativeCache() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public ValueWrapper get(Object key) {
        return new SimpleValueWrapper(this.arrayCache.get(key));
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void put(Object key, Object value) {
        assert value != null;
        this.arrayCache.put(key.toString(), value.toString());
    }

    @Override
    public void evict(Object key) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void clear() {
        this.arrayCache = new HashMap<>();
    }
}
