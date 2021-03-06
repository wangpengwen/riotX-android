/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.matrix.android.internal.crypto.model.rest

import com.squareup.moshi.Json

/**
 * Parent class representing an room key action request
 * Note: this class cannot be abstract because of [org.matrix.androidsdk.core.JsonUtils.toRoomKeyShare]
 */
open class RoomKeyShare : SendToDeviceObject {

    var action: String? = null

    @Json(name = "requesting_device_id")
    var requestingDeviceId: String? = null

    @Json(name = "request_id")
    var requestId: String? = null

    companion object {
        const val ACTION_SHARE_REQUEST = "request"
        const val ACTION_SHARE_CANCELLATION = "request_cancellation"
    }
}
