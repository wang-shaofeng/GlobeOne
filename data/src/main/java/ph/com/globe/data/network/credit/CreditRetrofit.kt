/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.credit

import ph.com.globe.model.credit.GetCreditInfoResponse
import ph.com.globe.model.credit.LoanPromoRequest
import retrofit2.Response
import retrofit2.http.*

interface CreditRetrofit {

    @GET("v2/creditManagement/accounts")
    suspend fun getCreditInfo(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>,
    ): Response<GetCreditInfoResponse>

    @POST("v2/creditManagement/customers/loan/promo")
    suspend fun loanPromo(
        @HeaderMap headers: Map<String, String>,
        @Body params: LoanPromoRequest,
    ): Response<Unit?>
}
