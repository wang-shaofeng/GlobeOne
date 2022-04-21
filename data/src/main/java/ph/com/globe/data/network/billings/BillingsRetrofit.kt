package ph.com.globe.data.network.billings

import ph.com.globe.model.billings.network_models.GetBillingsDetailsResponse
import ph.com.globe.model.billings.network_models.GetBillingsStatementsPdfResponse
import ph.com.globe.model.billings.network_models.GetBillingsStatementsResponse
import retrofit2.Response
import retrofit2.http.*

interface BillingsRetrofit {

    @GET("v3/accountManagement/accounts/billing")
    suspend fun getBillingsDetails(
        @HeaderMap headers: Map<String, String>,
        @Query("accountNumber") accountNumber: String?,
        @Query("landlineNumber") landlineNumber: String?,
        @Query("mobileNumber") mobileNumber: String?,
        @Query("brand") brand: String,
        @Query("segment") segment: String,
        @Query("accountType") accountType: String?
    ): Response<GetBillingsDetailsResponse>

    @GET("v2/customerAccount/billingStatements")
    suspend fun getBillingsStatements(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<GetBillingsStatementsResponse>

    @Streaming
    @GET("v2/customerAccount/billingStatements/{billingStatementId}")
    suspend fun getMobileBillingStatementsPdf(
        @HeaderMap headers: Map<String, String>,
        @Path("billingStatementId") billingStatementId: String,
        @Query("accountNumber") accountNumber: String?,
        @Query("mobileNumber") mobileNumber: String?,
        @Query("landlineNumber") landlineNumber: String?,
        @Query("segment") segment: String,
        @Query("format") accountType: String
    ): Response<GetBillingsStatementsPdfResponse>

    @Streaming
    @GET("v2/customerAccount/billingStatements")
    suspend fun getBroadbandBillingStatementsPdf(
        @HeaderMap headers: Map<String, String>,
        @Query("landlineNumber") landlineNumber: String?,
        @Query("segment") segment: String,
        @Query("format") accountType: String
    ): Response<GetBillingsStatementsPdfResponse>
}
