package ph.com.globe.data.network.prepaid

import ph.com.globe.model.prepaid.PrepaidLedgerResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query

interface PrepaidRetrofit {
    @GET("v1/accountManagement/transactions/{transactionType}")
    suspend fun getLoyaltySubscribersTransactionHistory(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "transactionType", encoded = false) transactionType: String,
        @Query("brand") brand: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("pageLimit") pageLimit: Int = 30,
        @Query("lastTransactionKey") lastTransactionKey: String? = null,
        @Query("mobileNumber") mobileNumber: String
    ): Response<PrepaidLedgerResponseModel>
}
