package io.th0rgal.packlayer.core;

/**
 * Functional interface for handling pack skip events.
 */
@FunctionalInterface
public interface PackSkipEventHandler {

    /**
     * Called when a pack skip is about to occur.
     * Handlers can cancel the skip by calling {@link PackSkipEvent#setCancelled(boolean)}.
     *
     * @param event the skip event
     */
    void onPackSkip(PackSkipEvent event);
}
