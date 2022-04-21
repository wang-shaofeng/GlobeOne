/*
 * Copyright (C) 2018 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.globeonesuperapp.utils.navigation;

import java.util.Objects;

import ph.com.globe.globeonesuperapp.utils.navigation.backstack.BackStack;
import ph.com.globe.globeonesuperapp.utils.navigation.history.History;
import ph.com.globe.globeonesuperapp.utils.navigation.keys.ScreenKey;

/**
 * Abstract implementation of {@link ScreenChanger} that implements diffing between previous and
 * new state of history and knows what screen should be detached/attached/removed/added.
 *
 * @param <Screen> type of object that implementation manages
 */
public abstract class AbstractScreenChanger<Key extends ScreenKey, Screen>
        implements ScreenChanger<Key, Screen> {

    @Override
    public void onInit(History<Key> history) {
        setNewHistory(history);
    }

    private void setNewHistory(History<Key> history) {
        Key newTop = history.peek();
        for (Key key : history) {
            Screen screen = instantiateObjectFromKey(key);
            addObject(screen, key);
            if (!newTop.equals(key)) {
                detach(screen);
            }
        }
    }

    /**
     * Detach/hide screen.
     */
    protected abstract void detach(Screen screen);

    /**
     * Attach/show screen.
     */
    protected abstract void attach(Screen screen);

    /**
     * Add screen with associated key. For fragment this will be fragmentTransaction.add(screen)
     * or for view this will be rootView.addView(screen)
     */
    protected abstract void addObject(Screen screen, Key key);

    /**
     * Remove screen with associated key. For fragment this will be fragmentTransaction.remove(screen)
     * or for view this will be rootView.removeView(screen)
     */
    protected abstract void removeObject(Screen screen, Key key);

    /**
     * Create new object associated with key.
     */
    protected abstract Screen instantiateObjectFromKey(Key key);

    @Override
    public void navigate(History<Key> previous, History<Key> newState, NavDirection direction,
                         BackStack.AnimOptions animOptions) {
        switch (direction) {
            case FORWARD: {
                Key oldTop = previous.peek();
                if (oldTop != null) {
                    detach(Objects.requireNonNull(getObjectForKey(oldTop)));
                }
                History<Key> difference = newState.topDifference(previous);
                Key newTop = newState.peek();
                for (Key key : difference) {
                    Screen screen = instantiateObjectFromKey(key);
                    addObject(screen, key);
                    if (!newTop.equals(key)) {
                        detach(screen);
                    }
                }
            }
            break;
            case BACKWARD: {
                History<Key> topDiff = previous.topDifference(newState);
                for (Key key : topDiff) {
                    removeObject(Objects.requireNonNull(getObjectForKey(key)), key);
                }
                if (!newState.isEmpty()) {
                    Screen newTopScreen = getObjectForKey(newState.peek());
                    attach(newTopScreen);
                }
            }
            break;
            case REORDER: {
                Key newTop = newState.peek();
                Key oldTop = previous.peek();
                detach(Objects.requireNonNull(getObjectForKey(oldTop)));
                attach(Objects.requireNonNull(getObjectForKey(newTop)));
            }
            break;
            case REPLACE: {
                for (Key key : previous) {
                    Screen screen = getObjectForKey(key);
                    removeObject(screen, key);
                }
                setNewHistory(newState);
            }
            break;

            case RESET: {
                if (newState.size() != 1)
                    throw new IllegalArgumentException("New history can only have single key");
                Key newSingleKey = newState.peek();

                if (previous.contains(newSingleKey)) {

                    History<Key> toRemove;
                    if (previous.peek().equals(newSingleKey)) {
                        toRemove = previous.pop();
                    } else {
                        toRemove = previous.bringToTop(newSingleKey).pop();
                        attach(Objects.requireNonNull(getObjectForKey(newSingleKey)));
                    }

                    for (Key tail : toRemove) {
                        removeObject(Objects.requireNonNull(getObjectForKey(tail)), tail);
                    }

                } else {
                    navigate(previous, newState, NavDirection.REPLACE, animOptions);
                }
            }
            break;

            default:
                throw new IllegalStateException("Unhandled direction type = " + direction);
        }
    }
}
