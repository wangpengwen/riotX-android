/*
 * Copyright 2016 OpenMarket Ltd
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
import com.squareup.moshi.JsonClass

/**
 * This class represents the response to /keys/upload request made by uploadKeys.
 */
@JsonClass(generateAdapter = true)
data class KeysUploadResponse(

        /**
         * The count per algorithm as returned by the home server: a map (algorithm to count).
         */
        @Json(name = "one_time_key_counts")
        var oneTimeKeyCounts: Map<String, Int>? = null

) {
    /**
     * Helper methods to extract information from 'oneTimeKeyCounts'
     *
     * @param algorithm the expected algorithm
     * @return the time key counts
     */
    fun oneTimeKeyCountsForAlgorithm(algorithm: String): Int {
        return oneTimeKeyCounts?.get(algorithm) ?: 0
    }

    /**
     * Tells if there is a oneTimeKeys for a dedicated algorithm.
     *
     * @param algorithm the algorithm
     * @return true if it is found
     */
    fun hasOneTimeKeyCountsForAlgorithm(algorithm: String): Boolean {
        return oneTimeKeyCounts?.containsKey(algorithm) == true
    }
}
