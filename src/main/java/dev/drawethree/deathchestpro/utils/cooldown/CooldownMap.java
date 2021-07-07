package dev.drawethree.deathchestpro.utils.cooldown;

import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

public interface CooldownMap<T> {

    /**
     * Creates a new collection with the cooldown properties defined by the base instance
     *
     * @param base the cooldown to base off
     * @return a new collection
     */
    static <T> CooldownMap<T> create(Cooldown base) {
        Objects.requireNonNull(base, "base");
        return new CooldownMapImpl<>(base);
    }

    /**
     * Gets the base cooldown
     *
     * @return the base cooldown
     */
    Cooldown getBase();

    /**
     * Gets the internal cooldown instance associated with the given key.
     *
     * <p>The inline Cooldown methods in this class should be used to access the functionality of the cooldown as opposed
     * to calling the methods directly via the instance returned by this method.</p>
     *
     * @param key the key
     * @return a cooldown instance
     */
    Cooldown get(T key);

    void put(T key, Cooldown cooldown);

    /**
     * Gets the cooldowns contained within this collection.
     *
     * @return the backing map
     */
    Map<T, Cooldown> getAll();

    /* methods from Cooldown */

    default boolean test(T key) {
        return get(key).test();
    }

    default boolean testSilently(T key) {
        return get(key).testSilently();
    }

    default long elapsed(T key) {
        return get(key).elapsed();
    }

    default void reset(T key) {
        get(key).reset();
    }

    default long remainingMillis(T key) {
        return get(key).remainingMillis();
    }

    default long remainingTime(T key, TimeUnit unit) {
        return get(key).remainingTime(unit);
    }

    default OptionalLong getLastTested(T key) {
        return get(key).getLastTested();
    }

    default void setLastTested(T key, long time) {
        get(key).setLastTested(time);
    }

}