/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.profile_info

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import ph.com.globe.model.profile.response_models.GetRegisteredUserResponseResult
import ph.com.globe.model.profile.domain_models.RegisteredUser
import ph.com.globe.model.profile.domain_models.RegisteredUserAddress

@Entity(tableName = RegisteredUserEntity.TABLE_NAME)
@JsonClass(generateAdapter = true)
data class RegisteredUserEntity(
    val emailVerificationDate: String? = null,
    @PrimaryKey(autoGenerate = false)
    val email: String,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val suffix: String? = null,
    val birthdate: String? = null,
    val salutation: String? = null,
    val contactNumber: String? = null,
    val address: RegisteredUserAddressEntity? = null
) {
    companion object {
        const val TABLE_NAME = "registered_user"
    }
}

@JsonClass(generateAdapter = true)
data class RegisteredUserAddressEntity(
    val province: String? = null,
    val city: String? = null,
    val barangay: String? = null,
    val street: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val postal: String? = null
)

fun GetRegisteredUserResponseResult.toEntity() =
    RegisteredUserEntity(
        emailVerificationDate,
        email,
        firstName,
        middleName,
        lastName,
        nickname,
        suffix,
        birthdate,
        salutation,
        contactNumber,
        RegisteredUserAddressEntity(
            address?.province,
            address?.city,
            address?.barangay,
            address?.street,
            address?.addressLine1,
            address?.addressLine2,
            address?.postal
        )
    )

fun RegisteredUserEntity.toDomain() =
    RegisteredUser(
        emailVerificationDate,
        email,
        firstName,
        middleName,
        lastName,
        nickname,
        suffix,
        birthdate,
        salutation,
        contactNumber,
        RegisteredUserAddress(
            address?.province,
            address?.city,
            address?.barangay,
            address?.street,
            address?.addressLine1,
            address?.addressLine2,
            address?.postal
        )
    )
