/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rush.usecases

import ph.com.globe.domain.rush.RushDataManager
import ph.com.globe.domain.user_details.UserDetailsDataManager
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.rush.GetRushAccessTokenError
import ph.com.globe.model.rush.CreateRushUserParams
import ph.com.globe.model.rush.GetRushAccessTokenResponseModel
import ph.com.globe.model.rush.GetRushUserAccessTokenParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold

class CreateRushUserIfNeededAndGetUserAccessTokenUseCase constructor(
    private val rushDataManager: RushDataManager,
    private val userDetailsDataManager: UserDetailsDataManager
) {

    suspend fun execute(): LfResult<GetRushAccessTokenResponseModel, GetRushAccessTokenError> {
        var email = ""
        userDetailsDataManager.getEmail().fold(
            {
                email = it
            }, {
                return LfResult.failure(GetRushAccessTokenError.General(GeneralError.General))
            }
        )
        //try to get rush user access token to verify spinwheel will work when started
        rushDataManager.getRushUserAccessToken(GetRushUserAccessTokenParams(email)).fold(
            {
                return LfResult.success(it)
            },
            {
                //if getting the ussr access token failed, we need to create the user first
                rushDataManager.getRushAdminAccessToken().fold({ tokenResponseModel ->
                    rushDataManager.createRushUser(
                        CreateRushUserParams(
                            tokenResponseModel.access_token,
                            email
                        )
                    ).fold({
                        //finally we try again to get user access token to confirm user has
                        //been created
                        rushDataManager.getRushUserAccessToken(
                            GetRushUserAccessTokenParams(
                                email
                            )
                        ).fold({
                            return LfResult.success(it)
                        }, {
                            return LfResult.failure(it)
                        })
                    }, {
                        return LfResult.failure(GetRushAccessTokenError.General(GeneralError.General))
                    })
                }, {
                    //if we couldn't get admin access token, we can't create the user
                    return LfResult.failure(it)
                })
            }
        )
    }
}
