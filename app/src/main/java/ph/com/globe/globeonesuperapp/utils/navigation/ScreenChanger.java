/*
 * Copyright (C) 2018 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.globeonesuperapp.utils.navigation;

import android.os.Bundle;

import androidx.annotation.MainThread;

import ph.com.globe.globeonesuperapp.utils.navigation.backstack.BackStack;
import ph.com.globe.globeonesuperapp.utils.navigation.history.History;
import ph.com.globe.globeonesuperapp.utils.navigation.keys.IScreenKey;

/**
 * While {@link BackStack} implements back stack and policy for navigation, this interface
 * implements mechanism.
 * So back stack policy could be changed independently of underlying mechanism, be it fragments
 * or vies etc.
 */
@MainThread
public interface ScreenChanger<Key extends IScreenKey, T> {

    /**
     * Get object (fragment, view, etc) managed by this backstack by key.
     */
    T getObjectForKey(Key key);

    /**
     * Save state, if any, that should be restore on config change, here.
     */
    void onSaveInstanceState(History<Key> history, Bundle outState);

    /**
     * This is called when {@link BackStack} is first time created, and there's no previous state.
     */
    void onInit(History<Key> history);

    /**
     * This is called when {@link BackStack} is created and there's previous state from which we
     * should restore
     */
    void onReInit(History<Key> history, Bundle savedState);

    /**
     * Given previous, new state and direction add/remove/change (as fit) screens so that
     * underlying implementation is in sync with new state.
     * <p>
     * So for example if we have previous keys [2, 1] (1 on top), and new state [3,2,1], and
     * direction FORWARD then implementation should hide/detach screen reprsented by Key 2 and
     * create <b>new</b> screen for Key 3 and show it to the user.
     * <b>Note</b> differnece between new and old state could be more than one fragment
     * <p>
     * REPLACE should remove all previous screens and net up new screens from new state.
     * <p>
     * BACKWARD is same as FORWARD but in opposite direction, it should remove screens from top
     * of the previous state, and attach top from new state.
     * <b>Note</b> differnece between new and old state could be more than one fragment
     * <p>
     * REORDER should <b>not</b> create or remove screens just reorder them, that is detach/hide
     * previous top and attach new top.
     * <p>
     * RESET should remove all but the single screen, if it exists, else It's same as REPLACE with single screen.
     * <p>
     * NOTE: Keep in mind that only one screen should be shown to the user at one moment
     */
    void navigate(History<Key> previous, History<Key> newState,
                  NavDirection direction,
                  BackStack.AnimOptions animOptions);

    /**
     * If concrete impl of this back stack is using asynchronous commits this will return true
     * while waiting for commit to finish.
     */
    boolean waitingForCommit();

    /**
     * Internal use. Defines what operation should be done.
     */
    enum NavDirection {
        /**
         * Pushing key to the top of the history
         */
        FORWARD,
        /**
         * Popping key from the top of the history
         */
        BACKWARD,
        /**
         * Replacing history. Deletes all history and setup new
         */
        REPLACE,
        /**
         * History is same as before but order is different.
         */
        REORDER,
        /**
         * Same as replace but will not remove the key if it already exists in backstack.
         * Will always leave one single key on the stack. If key is not on the stack it will be
         * recreated.
         */
        RESET
    }
}
