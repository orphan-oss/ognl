package ognl.internal;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.util.concurrent.ConcurrentHashMap;

public class LazyCache<KK, VV> {

    private final Function<KK, VV> lookup;
    private final ConcurrentHashMap<KK, Optional<VV>> cache = new ConcurrentHashMap<KK, Optional<VV>>();

    public LazyCache(Function<KK,VV> lookup) {
        this.lookup = lookup;
    }

    public VV get(KK key) {
        Optional<VV> value = cache.get(key);
        if(value != null) {
            if(!value.isPresent())
                return null;
            return value.get();
        }

        VV lookedUp = lookup.apply(key);

        if(lookedUp == null) {
            cache.put(key, Optional.<VV>absent());
        } else {
            cache.put(key, Optional.of(lookedUp));
        }

        return lookedUp;
    }
}
