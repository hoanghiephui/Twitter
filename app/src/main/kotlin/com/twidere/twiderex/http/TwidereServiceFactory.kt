/*
 *  Twidere X
 *
 *  Copyright (C) 2020-2021 Tlaster <tlaster@outlook.com>
 * 
 *  This file is part of Twidere X.
 * 
 *  Twidere X is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Twidere X is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Twidere X. If not, see <http://www.gnu.org/licenses/>.
 */
package com.twidere.twiderex.http

import com.twidere.services.http.HttpClientFactory
import com.twidere.services.http.config.HttpConfigClientFactory
import com.twidere.services.mastodon.MastodonService
import com.twidere.services.microblog.MicroBlogService
import com.twidere.services.twitter.TwitterService
import com.twidere.twiderex.model.PlatformType
import com.twidere.twiderex.model.cred.Credentials
import com.twidere.twiderex.model.cred.OAuth2Credentials
import com.twidere.twiderex.model.cred.OAuthCredentials

class TwidereServiceFactory(private val configProvider: TwidereHttpConfigProvider) {

    companion object {
        private var instance: TwidereServiceFactory? = null

        fun initiate(configProvider: TwidereHttpConfigProvider) {
            instance = TwidereServiceFactory(configProvider)
        }

        fun createApiService(type: PlatformType, credentials: Credentials, host: String = ""): MicroBlogService {
            return instance?.let {
                when (type) {
                    PlatformType.Twitter -> {
                        credentials.let {
                            it as OAuthCredentials
                        }.let {
                            TwitterService(
                                consumer_key = it.consumer_key,
                                consumer_secret = it.consumer_secret,
                                access_token = it.access_token,
                                access_token_secret = it.access_token_secret,
                                httpClientFactory = createHttpClientFactory()
                            )
                        }
                    }
                    PlatformType.StatusNet -> TODO()
                    PlatformType.Fanfou -> TODO()
                    PlatformType.Mastodon ->
                        credentials.let {
                            it as OAuth2Credentials
                        }.let {
                            MastodonService(
                                host,
                                it.access_token,
                                httpClientFactory = createHttpClientFactory()
                            )
                        }
                }
            } ?: throw Error("Factory needs to be initiate")
        }

        fun createHttpClientFactory(): HttpClientFactory {
            return instance?.let {
                HttpConfigClientFactory(it.configProvider)
            } ?: throw Error("Factory needs to be initiate")
        }
    }
}
