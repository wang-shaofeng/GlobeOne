/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.maintenance

import ph.com.globe.model.maintenance.MaintenanceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface MaintenanceRetrofit {

    @GET("/api/mobile/route/{landingPath}")
    suspend fun getMaintenance(
        @Path(value = "landingPath") landingPath: String,
        @QueryMap query: Map<String, String>
    ): Response<MaintenanceResponse>
}
