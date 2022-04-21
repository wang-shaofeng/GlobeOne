/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.inputcontroller

object Preconditions {

    @Throws(InstantiationException::class)
    private fun Preconditions() {
        throw InstantiationException("This class is not to be instantiated!")
    }

    /**
     * Checks whether the given object is null.
     * If the object is null, this will throw an [IllegalArgumentException]
     * with a default message.
     *
     * @param object The object to be checked.
     */
    fun <T> checkNotNull(`object`: T): T {
        return checkNotNull(`object`, "This parameter should not be null")
    }

    /**
     * Checks whether the given object is null.
     * If the object is null, this will throw an [IllegalArgumentException]
     * along with the supplied message.
     *
     * @param object  The object to be checked.
     * @param message The message to be supplied to the exception if the object is null.
     */
    fun <T> checkNotNull(`object`: T?, message: String?): T {
        requireNotNull(`object`) { message!! }
        return `object`
    }

    /**
     * Checks whether the expression is true.
     * If not, this will throw an [IllegalArgumentException].
     *
     * @param object  This object is returned without any method call or modification. It's just
     * passed so it's possible to write
     * <pre>this.arg = Preconditions.checkArgument(arg, someCheck());</pre>
     */
    fun <T> checkArgument(`object`: T, expression: Boolean): T {
        require(expression) { "Object: $`object` doesn't satisfy expression" }
        return `object`
    }

    fun checkArgument(expression: Boolean) {
        checkArgument<Any?>(null, expression)
    }
}