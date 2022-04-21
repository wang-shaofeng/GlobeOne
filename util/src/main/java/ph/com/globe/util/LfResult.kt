/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.util

/**
 * Used to wrap results of an operation that can be successful or unsuccessful.
 * At least one of the values (value or error) must be non-null.
 *
 * @param <S> Result of an operation.
 * @param <F> Type of error that can occur when the operation is executed.
 */
data class LfResult<out S, out F> private constructor(
    val value: S?,
    val error: F?
) {
    fun successOrNull(): S? = value

    fun errorOrNull(): F? = error

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LfResult<*, *>

        if (value != other.value) return false
        if (error != other.error) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }

    companion object {

        @JvmName("success")
        fun <S, F> success(value: S): LfResult<S, F> = LfResult(value, null)

        @JvmName("failure")
        fun <S, F> failure(fail: F, value: S? = null): LfResult<S, F> = LfResult(value, fail)
    }
}

inline fun <R, F, S : R> LfResult<S, F>.successOrErrorAction(errorAction: (error: F) -> R): R =
    when (val exception = error) {
        null -> value as S
        else -> errorAction(exception)
    }

inline fun <R, S, F> LfResult<S, F>.fold(
    successAction: (value: S) -> R,
    failureAction: (exception: F) -> R,
    finally: ((LfResult<S, F>) -> Unit) = {}
): R =
    when (val exception = error) {
        null -> successAction(value as S)
        else -> failureAction(exception)
    }.also { finally(this) }

/**
 * Performs the given [action] if the result of the [LfResult] represents successful value.
 * The [LfResult]'s success value is passed to the [action] lambda. Original [LfResult]
 * is returned for easier chaining.
 */
inline fun <S, F> LfResult<S, F>.onSuccess(action: (success: S) -> Unit): LfResult<S, F> {
    if (error == null) value?.let(action)
    return this
}

/**
 * Performs the given [action] if the result of the [LfResult] represents error value.
 * The [LfResult]'s error value is passed to the [action] lambda. Original [LfResult]
 * is returned for easier chaining.
 */
inline fun <S, F> LfResult<S, F>.onFailure(action: (fail: F) -> Unit): LfResult<S, F> {
    error?.let(action) // TODO: It would be better to return pair of error and value, so we should refactor this.
    return this
}
