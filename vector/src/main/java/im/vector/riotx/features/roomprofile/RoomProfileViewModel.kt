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
 *
 */

package im.vector.riotx.features.roomprofile

import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import im.vector.matrix.android.api.MatrixCallback
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.rx.rx
import im.vector.matrix.rx.unwrap
import im.vector.riotx.R
import im.vector.riotx.core.platform.VectorViewModel
import im.vector.riotx.core.resources.StringProvider
import im.vector.riotx.core.utils.DataSource
import im.vector.riotx.core.utils.PublishDataSource
import im.vector.riotx.core.viewevents.CommonViewEvents

class RoomProfileViewModel @AssistedInject constructor(@Assisted initialState: RoomProfileViewState,
                                                       private val stringProvider: StringProvider,
                                                       private val session: Session)
    : VectorViewModel<RoomProfileViewState, RoomProfileAction>(initialState) {

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: RoomProfileViewState): RoomProfileViewModel
    }

    companion object : MvRxViewModelFactory<RoomProfileViewModel, RoomProfileViewState> {

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: RoomProfileViewState): RoomProfileViewModel? {
            val fragment: RoomProfileFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.roomProfileViewModelFactory.create(state)
        }
    }

    private val _profileViewEvents = PublishDataSource<RoomProfileViewEvents>()
    val profileViewEvents: DataSource<RoomProfileViewEvents> = _profileViewEvents

    private val room = session.getRoom(initialState.roomId)!!

    init {
        observeRoomSummary()
    }

    private fun observeRoomSummary() {
        room.rx().liveRoomSummary()
                .unwrap()
                .execute {
                    copy(roomSummary = it)
                }
    }

    override fun handle(action: RoomProfileAction) = when (action) {
        RoomProfileAction.LeaveRoom                      -> handleLeaveRoom()
        is RoomProfileAction.ChangeRoomNotificationState -> handleChangeNotificationMode(action)
    }

    private fun handleChangeNotificationMode(action: RoomProfileAction.ChangeRoomNotificationState) {
        room.setRoomNotificationState(action.notificationState, object : MatrixCallback<Unit> {
            override fun onFailure(failure: Throwable) {
                _viewEvents.post(CommonViewEvents.Failure(failure))
            }
        })
    }

    private fun handleLeaveRoom() {
        _viewEvents.post(CommonViewEvents.Loading(stringProvider.getString(R.string.room_profile_leaving_room)))
        room.leave(null, object : MatrixCallback<Unit> {
            override fun onSuccess(data: Unit) {
                _profileViewEvents.post(RoomProfileViewEvents.OnLeaveRoomSuccess)
            }

            override fun onFailure(failure: Throwable) {
                _viewEvents.post(CommonViewEvents.Failure(failure))
            }
        })
    }
}
