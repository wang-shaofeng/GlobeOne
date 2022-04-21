/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.group

import ph.com.globe.model.group.*
import retrofit2.Response
import retrofit2.http.*

interface GroupRetrofit {

    @GET("v3/poolManagement/groups")
    suspend fun getGroupList(
        @HeaderMap headers: Map<String, String>,
        @Query(value = "accountAlias") accountAlias: String? = null,
        @Query(value = "isGroupOwner") isGroupOwner: Boolean? = null,
        @Query(value = "groupName") groupName: String? = null
    ): Response<GetGroupListResponse>

    @POST("v3/poolManagement/groups/{groupId}/members")
    suspend fun addGroupMember(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "groupId", encoded = false) groupId: String,
        @Body addGroupMemberNetworkParams: AddGroupMemberNetworkParams
    ): Response<AddGroupMemberResponse>

    @GET("v3/usageConsumption/groups")
    suspend fun retrieveGroupUsage(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<RetrieveGroupUsageResponse>

    @GET("v3/usageConsumption/members")
    suspend fun retrieveMemberUsage(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<RetrieveMemberUsageResponse>

    @GET("v3/usageConsumption/groups/service")
    suspend fun retrieveGroupService(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<RetrieveGroupServiceResponse>

    @HTTP(method = "DELETE", path = "v3/poolManagement/groups/{groupId}/members", hasBody = true)
    suspend fun deleteGroupMember(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "groupId", encoded = false) groupId: String,
        @Body removeMemberParams: DeleteGroupMemberNetworkParams
    ): Response<DeleteGroupMemberResponse>

    @POST("v3/usageConsumption/members/usageLimit")
    suspend fun setMemberUsageLimit(
        @HeaderMap headers: Map<String, String>,
        @Body setMemberUsageLimitParams: SetMemberUsageLimitParams
    ): Response<Unit?>
}
