/*
 * Copyright (C) 2018 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.globeonesuperapp.utils.navigation.backstack;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.AnimatorRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import java.util.Collection;

import ph.com.globe.globeonesuperapp.utils.navigation.NavigationListenersHelper;
import ph.com.globe.globeonesuperapp.utils.navigation.ScreenChanger;
import ph.com.globe.globeonesuperapp.utils.navigation.ScreenChanger.NavDirection;
import ph.com.globe.globeonesuperapp.utils.navigation.history.History;
import ph.com.globe.globeonesuperapp.utils.navigation.history.HistoryBuilder;
import ph.com.globe.globeonesuperapp.utils.navigation.keys.IScreenKey;

/**
 * Class that manages back stack of {@link IScreenKey keys}, which represent screen in application.
 * Key can correspond to fragments, views or some other construct.
 * <p>
 * To use this class instantiate it in Activity/Fragment and delegate calls
 * onCreate/onSaveInstanceState/onBackPressed.
 *
 * @param <Key> Type of the key this backstack is going to manage.
 * @param <T>   Object type that corresponds to key (ex. fragment)
 */
@MainThread
public class BackStack<Key extends IScreenKey, T> {
    private static final String TAG = "BackStack";
    private static final String SCREEN_CHANGE_STATE = TAG + ".screenChangerState";

    /**
     * Internal use only interface, for use in
     * {@link #callNavigate(NavDirection, AnimOptions, boolean, HistoryReWriter)}
     */
    @FunctionalInterface
    private interface HistoryReWriter<Key extends IScreenKey> {
        History<Key> newHistory(History<Key> old);
    }

    /**
     * Mode that specifies what should happen whn user uses {@link #goTo(IScreenKey)} method and
     * key is already on the stack
     */
    public enum HistoryMode {
        /**
         * Reorder item to the top instead of popping back to it.
         *
         * @see HistoryMode
         */
        REORDER_TO_TOP,
        /**
         * Go back to the key by popping back stack
         *
         * @see HistoryMode
         */
        GO_BACK
    }

    /**
     * Listener for listening for navigation events.
     */
    public interface NavigationListener<Key extends IScreenKey> {
        /**
         * @param previousState null if dispatching when registering listener
         * @param history       current state of back stack
         */
        void onNavigated(@Nullable History<Key> previousState, History<Key> history);
    }

    /**
     * Helps up manage listeners and dispatch to them.
     */
    private final NavigationListenersHelper<Key> navigationListenersHelper =
            new NavigationListenersHelper<>(this);
    private final ScreenChanger<Key, T> screenChanger;
    private final HistoryMode mode;
    private final AnimOptions defaultAnim;
    private boolean inTransaction = false;

    // mutable state -NavigationListenersHelper saved in onSaveInstaceState
    private History<Key> backStack;

    public BackStack(History<Key> startState,
                     ScreenChanger<Key, T> screenChanger) {
        this(startState, screenChanger, HistoryMode.GO_BACK);
    }

    public BackStack(History<Key> startState,
                     ScreenChanger<Key, T> screenChanger,
                     HistoryMode mode) {
        if (startState.isEmpty()) {
            throw new IllegalStateException();
        }
        this.screenChanger = screenChanger;
        backStack = startState;
        this.mode = mode;
        defaultAnim = new AnimOptions(android.R.animator.fade_in, android.R.animator.fade_out);
    }

    /**
     * Add listener to listen to navigation events.
     * Current state is immediately dispatched.
     *
     * @return same listener you passed in. Useful for lambdas as listeners
     */
    public NavigationListener<Key> addNavigationListener(NavigationListener<Key> listener) {
        return navigationListenersHelper.addNavigationListener(listener);
    }

    /**
     * Register listener that will be automatically removed when activity/fragment is destroyed.
     * Current state is immediately dispatched.
     *
     * @return listener just added so you cen remove it after if you're using lambdas
     */
    @SuppressWarnings("UnusedReturnValue")
    public NavigationListener<Key> addNavigationListener(LifecycleOwner lifecycleOwner,
                                                         NavigationListener<Key> listener) {
        return navigationListenersHelper.addNavigationListener(lifecycleOwner, listener);
    }

    /**
     * Remove navigation listener from this back stack
     */
    public void removeNavigationListener(NavigationListener<Key> navListener) {
        navigationListenersHelper.removeNavigationListener(navListener);
    }

    /**
     * Check if this back stack is currently dispatching navigation events to passed listener.
     * You should use this to prevent infinite recursive loop.
     * <p>
     * Example of this is if you have navigation listener that sets selected item in bottom bar,
     * but because of implementation of bottom bar widget it will call item selected listener and
     * you will not know if it is because of user clicked in ui or because you set selected item
     * programmatically. So you can use this method to check for that.
     * <p>
     * How to use:
     * <pre>
     *      private class NavCallbacks implements OnNavigationItemSelectedListener,
     *           , BackStack.NavigationListener {
     *
     *         {@literal @}Override
     *         public boolean onNavigationItemSelected(MenuItem menuItem) {
     *             if (backStack.isDispatchingTo(this)) {
     *                 return true;
     *             }
     *             // .. do your thing
     *         }
     *
     *         {@literal @}Override
     *         public void onNavigated({History previousState, History history) {
     *             if (!history.isEmpty()) {
     *                 long id = history.peek().getId();
     *                 if (navBar.getSelectedItemId() != id) {
     *                     navBar.setSelectedItemId((int) id);
     *                 }
     *             }
     *         }
     *      }
     * </pre>
     *
     * @param listener listener to check
     */
    public boolean isDispatchingTo(NavigationListener<Key> listener) {
        return navigationListenersHelper.isDispatchingTo(listener);
    }

    /**
     * Same as {@link #isDispatchingTo(NavigationListener)} but doesn't check for specific listener.
     */
    public boolean isDispatching() {
        return navigationListenersHelper.isDispatching();
    }

    /**
     * Dispatches new backstack state to all listeners.
     *
     * @param previousState Previous state of the backstack, current state will be get directly
     *                      from field.
     */
    protected void dispatchNavigationChanged(History<Key> previousState) {
        navigationListenersHelper.dispatchNavigationChanged(previousState);
    }

    protected Collection<? extends NavigationListener<Key>> navigationListeners() {
        return navigationListenersHelper.listeners();
    }

    /**
     * Options for navigation.
     */
    public static class NavOptions {
        private final boolean replaceIfOnTop;
        private final AnimOptions animOptions;

        public NavOptions(boolean replaceIfOnTop) {
            this(replaceIfOnTop, AnimOptions.NO_ANIM);
        }

        public NavOptions(AnimOptions animOptions) {
            this(false, animOptions);
        }

        public NavOptions(boolean replaceIfOnTop, AnimOptions animOptions) {
            this.replaceIfOnTop = replaceIfOnTop;
            this.animOptions = animOptions;
        }
    }

    /**
     * Enter and exit animation specification
     */
    public static class AnimOptions {
        public static final int NO_ANIM_RES = 0;
        private static final AnimOptions NO_ANIM = new AnimOptions(NO_ANIM_RES, NO_ANIM_RES);

        @AnimatorRes
        public final int enterAnim;
        @AnimatorRes
        public final int exitAnim;

        public AnimOptions(@AnimatorRes int enterAnim, @AnimatorRes int exitAnim) {
            this.enterAnim = enterAnim;
            this.exitAnim = exitAnim;
        }
    }

    /**
     * Get object (fragment, view, etc) managed by this backstack by key.
     */
    public T getObjectForKey(Key key) {
        return screenChanger.getObjectForKey(key);
    }

    /**
     * Get current back stack. Back stack is not 'live', that is it will not be updated when it's
     * changed.
     * Register {@link NavigationListener} to get navigation updates.
     */
    public History<Key> history() {
        return backStack;
    }

    /**
     * Use this to remove boilerplate from other methods that wnat to call
     * {@link ScreenChanger#navigate(History, History, NavDirection, AnimOptions)}.
     * <p>
     * It will use current {@link #backStack} as param for historyRewriter, to get new state.
     * After that it will call navigate and pass previous and new backstack.
     * New state will be assigned to {@link #backStack} field.
     * <p>
     * If callListeners is true it will call all listeners by calling {@link #dispatchNavigationChanged}
     *
     * @param direction       direction of navigation. Check {@link NavDirection}
     * @param animOptions     animation options
     * @param callListeners   Should listeners be called or not? If you need to do multiple
     *                        navigate calls, then this should be false so only last state is
     *                        dispatched but not intermediate.
     * @param historyRewriter Functional interface that gets current state and produces new state
     *                        of backstack.
     * @return previous state of backstack
     */
    private History<Key> callNavigate(NavDirection direction,
                                      AnimOptions animOptions,
                                      boolean callListeners,
                                      HistoryReWriter<Key> historyRewriter) {
        History<Key> previous = backStack;
        backStack = historyRewriter.newHistory(previous);
        screenChanger.navigate(previous, backStack, direction, animOptions);
        if (callListeners) {
            dispatchNavigationChanged(previous);
        }
        return previous;
    }

    /**
     * Call this to handler back button (from {@link Activity#onBackPressed()}).
     *
     * @return {@code true} if back press is handled. {@code false} otherwise, in that case
     * delegate back to higher level component.
     */
    public boolean onBackPressed() {
        Key top = backStack.peek();
        if (top != null) {
            if (backStack.size() == 1) {
                // last obj
                return false;
            }
            callNavigate(ScreenChanger.NavDirection.BACKWARD, defaultAnim, true, History::pop);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Call this from activity/fragment onCreate method to restore state of backstack.
     */
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            backStack = History.restoreFromState(savedInstanceState);
            Bundle screenChangeState = savedInstanceState.getBundle(SCREEN_CHANGE_STATE);

            if (screenChangeState == null) {
                throw new IllegalStateException("Restoring state. Have you forgot to call backstack.onSaveInstanceState?");
            }

            screenChangeState.setClassLoader(getClass().getClassLoader());
            screenChanger.onReInit(backStack, screenChangeState);
        } else {
            screenChanger.onInit(backStack);
        }
    }

    /**
     * Call this from activity/fragment onSaveInstanceState to save state of this back stack that
     * will be used when restoring from process death or configuration change.
     * Call this, otherwise app will end up in inconsistent state
     */
    public void onSaveInstanceState(Bundle outState) {
        backStack.saveToBundle(outState);
        Bundle bundle = new Bundle();
        screenChanger.onSaveInstanceState(backStack, bundle);
        outState.putBundle(SCREEN_CHANGE_STATE, bundle);
    }

    /**
     * Replaces top key in the back stack. If history is empty throws excpetion.
     */
    public void replaceTop(Key newTop) {
        if (backStack.isEmpty()) {
            throw new IllegalStateException("Cannot replace top as backstack is empty");
        }
        History<Key> startHistory = callNavigate(ScreenChanger.NavDirection.BACKWARD, defaultAnim, false,
                History::pop);
        callNavigate(ScreenChanger.NavDirection.FORWARD, defaultAnim, false, (b) -> b.push(newTop));
        dispatchNavigationChanged(startHistory);
    }

    /**
     * Replaces history. removes all keys and sets new ones.
     */
    public void replace(History<Key> newHistory) {
        callNavigate(ScreenChanger.NavDirection.REPLACE, defaultAnim, true, old -> newHistory);
    }

    /**
     * Resets history backstack to single key. If will not recreate key if it already exists.
     * If key doesn't exist then this call is equivalent to {@link #replace(History)}.
     */
    public void reset(@NonNull Key key) {
        callNavigate(ScreenChanger.NavDirection.RESET, defaultAnim, true, old -> History.singleton(key));
    }

    /**
     * Go back to the key if it exists
     *
     * @return true if key exists, false if not or already at that key
     */
    public boolean goBackTo(Key key) {
        if (!backStack.contains(key)) {
            return false;
        }
        if (key.equals(backStack.peek())) {
            return false;
        }
        HistoryBuilder<Key> builder = backStack.toBuilder();
        Key peek;
        while ((peek = builder.peek()) != null && !peek.equals(key)) {
            builder.pop();
        }

        callNavigate(ScreenChanger.NavDirection.BACKWARD, defaultAnim, true, old -> builder.build());
        return true;
    }

    /**
     * Go back to the key before specified if it exists.
     *
     * @return true if key exists, false if not
     */
    public boolean goBackBefore(Key key) {
        if (!backStack.contains(key)) {
            return false;
        }
        HistoryBuilder<Key> builder = backStack.toBuilder();
        Key peek;
        while ((peek = builder.peek()) != null && !peek.equals(key)) {
            builder.pop();
        }
        builder.pop();
        if (builder.peek() == null) {
            return false;
        }
        callNavigate(ScreenChanger.NavDirection.BACKWARD, defaultAnim, true, old -> builder.build());
        return true;
    }

    /**
     * @see #goTo(IScreenKey, NavOptions)
     */
    public void goTo(Key key) {
        goTo(key, new NavOptions(false));
    }

    /**
     * Goes to specified key. If it exists then goes back to it if mode is
     * {@link HistoryMode#GO_BACK},
     * popping back stack. If mode is {@link HistoryMode#REORDER_TO_TOP} than takes that key and
     * reorders
     * back stack so it end up on the top.
     * If key doesn't exists then puts it on the top.
     *
     * @param key where to go
     */
    public void goTo(Key key, NavOptions options) {
        inTransaction = true;
        Key peek = backStack.peek();
        if (peek != null && peek.equals(key) && options.replaceIfOnTop) {
            // remove
            screenChanger.navigate(backStack, backStack.pop(), ScreenChanger.NavDirection.BACKWARD,
                    options.animOptions);
            // then add again
            screenChanger.navigate(backStack.pop(), backStack.pop().push(key), ScreenChanger.NavDirection.FORWARD,
                    options.animOptions);
            return;
        }
        boolean contains = backStack.contains(key);
        if (contains) {
            switch (mode) {
                case GO_BACK:
                    // go back
                    goBackTo(key);
                    break;
                case REORDER_TO_TOP:
                    callNavigate(ScreenChanger.NavDirection.REORDER, options.animOptions, true, old -> old.bringToTop(key));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown mode: " + mode);
            }
        } else {
            callNavigate(ScreenChanger.NavDirection.FORWARD, options.animOptions, true, old -> old.push(key));
        }
    }

    /**
     * If concrete impl of this back stack is usingasynchronous commits this will return true
     * while waiting for commit to finish.
     */
    public boolean waitingForCommit() {
        return screenChanger.waitingForCommit();
    }

    public void screenIsAttached() {
        inTransaction = false;
    }

    public boolean isInTransaction() {
        return inTransaction;
    }

    public Key getCurrentKey() {
        return backStack.peek();
    }
}
