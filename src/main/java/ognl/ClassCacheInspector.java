package ognl;

/**
 * Optional interface that may be registered with {@link OgnlRuntime#setClassCacheInspector(ClassCacheInspector)} as
 * a means to disallow caching of specific class types.
 */
public interface ClassCacheInspector {

    /**
     * Invoked just before storing a class type within a cache instance.
     *
     * @param type
     *          The class that is to be stored.
     *
     * @return True if the class can be cached, false otherwise.
     */
    boolean shouldCache(Class type);
}
