/*
 * Copyright (C) 2018 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.globeonesuperapp.utils.navigation;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

/**
 * Helps execute nested transaction and apply common options to all of them.
 * It's possible to call {@link #doInTransaction(CallableNoException)} while in another block of
 * {@link #doInTransaction(CallableNoException)}. Both will share one transaction object, which
 * will be committed when both block are executed.
 */
@MainThread
public abstract class TransactionHelper<T> {
    public interface CallableNoException<T> {
        void call(T t);
    }

    /**
     * Implement this to be able to apply standard options for all transactions before commit.
     * @param <T> transaction type
     */
    public interface DefaultTransactionOptions<T> {
        void apply(T transaction);

        DefaultTransactionOptions<?> EMPTY = obj -> {
        };

        static <F> DefaultTransactionOptions<F> empty() {
            //noinspection unchecked
            return (DefaultTransactionOptions<F>) EMPTY;
        }
    }

    @Nullable
    private final Runnable delegatedOnCommitAction;
    private final DefaultTransactionOptions<T> defaultTransactionOptions;
    private final Runnable onCommitAction = new Runnable() {
        @Override
        public void run() {
            if (delegatedOnCommitAction != null) {
                new Handler(Looper.getMainLooper()).post(delegatedOnCommitAction);
            }
            waitingForCommitToFinish = false;
        }
    };

    // mutable state
    private int transactionDepth;
    private T currentTransaction;
    private boolean callingCommitGuard = false;
    private boolean waitingForCommitToFinish = false;

    public TransactionHelper(@Nullable Runnable delegatedOnCommitAction,
                             DefaultTransactionOptions<T> defaultTransactionOptions) {
        this.delegatedOnCommitAction = delegatedOnCommitAction;
        this.defaultTransactionOptions = defaultTransactionOptions;
    }

    TransactionHelper(DefaultTransactionOptions<T> defaultTransactionOptions) {
        this(null, defaultTransactionOptions);
    }

    TransactionHelper() {
        this(null, DefaultTransactionOptions.empty());
    }

    public boolean isWaitingForCommitToFinish() {
        return waitingForCommitToFinish;
    }

    /**
     * Starts transaction, calls your code, then apply common settings to transaction and commits it
     *
     * @param callable code to run
     */
    public void doInTransaction(CallableNoException<T> callable) {
        if (callingCommitGuard) {
            throw new IllegalStateException("cannot start another transaction while committing " +
                    "previous");
        }
        if (transactionDepth++ == 0) {
            currentTransaction = startTransaction();
        }
        try {
            callable.call(currentTransaction);
        } finally {
            if (--transactionDepth == 0) {
                callingCommitGuard = true;
                try {
                    commitTransaction(currentTransaction);
                    currentTransaction = null;
                } finally {
                    callingCommitGuard = false;
                }
            }
        }
    }

    private void commitTransaction(T transaction) {
        if (!shouldCommit(transaction)) {
            return;
        }
        defaultTransactionOptions.apply(transaction);
        setOnCommitActionToTransaction(transaction, onCommitAction);
        waitingForCommitToFinish = true;
        doCommit(transaction);
    }

    /**
     * Return new transaction object.
     */
    protected abstract T startTransaction();

    /**
     * Check if transaction should be commit or not. For example you might return true if
     * transaction is empty.
     */
    protected abstract boolean shouldCommit(T transaction);

    /**
     * When transaction is committed (if transaction is async, then this should be when
     * transaction is done) onCommitAction have to be called to do some post-commit actions.
     *
     * @param onCommitAction action that should run after transaction is fully committed
     */
    protected abstract void setOnCommitActionToTransaction(T transaction,
                                                           Runnable onCommitAction);

    /**
     * Commit transaction.
     */
    protected abstract void doCommit(T tr);
}
