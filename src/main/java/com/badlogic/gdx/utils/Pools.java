package com.badlogic.gdx.utils;

public class Pools {
    static private final ThreadLocal<ObjectMap<Class, Pool>> typePools = new ThreadLocal<ObjectMap<Class, Pool>>() {
        protected ObjectMap<Class, Pool> initialValue () {
            return new ObjectMap<Class, Pool>();
        };
    };

    /** Returns a new or existing pool for the specified type, stored in a Class to {@link Pool} map. Note the max size is ignored
     * if this is not the first time this pool has been requested. */
    static public <T> Pool<T> get (Class<T> type, int max) {
        Pool pool = typePools.get().get(type);
        if (pool == null) {
            pool = new ReflectionPool(type, 4, max);
            typePools.get().put(type, pool);
        }
        return pool;
    }

    /** Returns a new or existing pool for the specified type, stored in a Class to {@link Pool} map. The max size of the pool used
     * is 100. */
    static public <T> Pool<T> get (Class<T> type) {
        return get(type, 100);
    }

    /** Sets an existing pool for the specified type, stored in a Class to {@link Pool} map. */
    static public <T> void set (Class<T> type, Pool<T> pool) {
        typePools.get().put(type, pool);
    }

    /** Obtains an object from the {@link #get(Class) pool}. */
    static public <T> T obtain (Class<T> type) {
        return get(type).obtain();
    }

    /** Frees an object from the {@link #get(Class) pool}. */
    static public void free (Object object) {
        if (object == null) throw new IllegalArgumentException("object cannot be null.");
        Pool pool = typePools.get().get(object.getClass());
        if (pool == null) return; // Ignore freeing an object that was never retained.
        pool.free(object);
    }

    /** Frees the specified objects from the {@link #get(Class) pool}. Null objects within the array are silently ignored. Objects
     * don't need to be from the same pool. */
    static public void freeAll (Array objects) {
        freeAll(objects, false);
    }

    /** Frees the specified objects from the {@link #get(Class) pool}. Null objects within the array are silently ignored.
     * @param samePool If true, objects don't need to be from the same pool but the pool must be looked up for each object. */
    static public void freeAll (Array objects, boolean samePool) {
        if (objects == null) throw new IllegalArgumentException("objects cannot be null.");
        Pool pool = null;
        for (int i = 0, n = objects.size; i < n; i++) {
            Object object = objects.get(i);
            if (object == null) continue;
            if (pool == null) {
                pool = typePools.get().get(object.getClass());
                if (pool == null) continue; // Ignore freeing an object that was never retained.
            }
            pool.free(object);
            if (!samePool) pool = null;
        }
    }

    public static String name() {
        return "ThreadSafe Pools";
    }

    private Pools () {
    }
}
