/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.billings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.billings.di.BillingsComponent
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
import javax.inject.Inject

class BillingsUseCaseManager @Inject constructor(
    factory: BillingsComponent.Factory
) : BillingsDomainManager {

    private val billingsComponent = factory.create()

    override suspend fun getBillingsDetails(params: GetBillingsDetailsParams): LfResult<BillingsDetails, GetBillingsDetailsError> =
        withContext(Dispatchers.IO) {
            billingsComponent.provideGetBillingsDetailsUseCase().execute(params)
        }

    override suspend fun getBillingsStatements(params: GetBillingsStatementsParams): LfResult<List<BillingStatement>, GetBillingsStatementsError> =
        withContext(Dispatchers.IO) {
            billingsComponent.provideGetBillingsStatementsUseCase().execute(params)
        }

    override suspend fun getBillingsStatementsPdf(params: GetBillingsStatementsPdfParams): LfResult<GetBillingsStatementsPdfResponse, GetBillingsStatementsPdfError> =
        withContext(Dispatchers.IO) {
            billingsComponent.provideGetBillingsStatementsPdfUseCase().execute(params)
        }
}
