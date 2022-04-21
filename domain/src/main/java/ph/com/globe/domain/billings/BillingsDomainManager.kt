/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.billings

import ph.com.globe.errors.billings.GetBillingsDetailsError
import ph.com.globe.errors.billings.GetBillingsStatementsError
import ph.com.globe.errors.billings.GetBillingsStatementsPdfError
import ph.com.globe.model.billings.domain_models.BillingStatement
import ph.com.globe.model.billings.domain_models.BillingsDetails
import ph.com.globe.model.billings.network_models.GetBillingsDetailsParams
import ph.com.globe.model.billings.network_models.GetBillingsStatementsParams
import ph.com.globe.model.billings.network_models.GetBillingsStatementsPdfParams
import ph.com.globe.model.billings.network_models.GetBillingsStatementsPdfResponse
import ph.com.globe.util.LfResult

interface BillingsDomainManager {

    suspend fun getBillingsDetails(params: GetBillingsDetailsParams): LfResult<BillingsDetails, GetBillingsDetailsError>

    suspend fun getBillingsStatements(params: GetBillingsStatementsParams): LfResult<List<BillingStatement>, GetBillingsStatementsError>

    suspend fun getBillingsStatementsPdf(params: GetBillingsStatementsPdfParams): LfResult<GetBillingsStatementsPdfResponse, GetBillingsStatementsPdfError>
}
