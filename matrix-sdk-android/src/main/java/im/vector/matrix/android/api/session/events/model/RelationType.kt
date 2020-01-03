/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.matrix.android.api.session.events.model

/**
 * Constants defining known event relation types from Matrix specifications
 */
object RelationType {

    /** Lets you define an event which annotates an existing event.*/
    const val ANNOTATION = "m.annotation"
    /** Lets you define an event which replaces an existing event.*/
    const val REPLACE = "m.replace"
    /** Lets you define an event which references an existing event.*/
    const val REFERENCE = "m.reference"
    /** Lets you define an event which references an existing event.*/
    const val RESPONSE = "m.response"
}
