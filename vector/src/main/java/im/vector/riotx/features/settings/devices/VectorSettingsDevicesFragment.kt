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

package im.vector.riotx.features.settings.devices

import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import im.vector.matrix.android.internal.crypto.model.rest.DeviceInfo
import im.vector.riotx.R
import im.vector.riotx.core.extensions.cleanup
import im.vector.riotx.core.extensions.configureWith
import im.vector.riotx.core.extensions.observeEvent
import im.vector.riotx.core.platform.VectorBaseActivity
import im.vector.riotx.core.platform.VectorBaseFragment
import im.vector.riotx.core.utils.DataSource
import im.vector.riotx.core.utils.toast
import im.vector.riotx.core.viewevents.CommonViewEvents
import kotlinx.android.synthetic.main.fragment_generic_recycler.*
import kotlinx.android.synthetic.main.merge_overlay_waiting_view.*
import javax.inject.Inject

/**
 * Display the list of the user's device
 */
class VectorSettingsDevicesFragment @Inject constructor(
        val devicesViewModelFactory: DevicesViewModel.Factory,
        private val devicesController: DevicesController
) : VectorBaseFragment(), DevicesController.Callback {

    // used to avoid requesting to enter the password for each deletion
    private var mAccountPassword: String = ""

    override fun getLayoutResId() = R.layout.fragment_generic_recycler

    private val viewModel: DevicesViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        waiting_view_status_text.setText(R.string.please_wait)
        waiting_view_status_text.isVisible = true
        devicesController.callback = this
        recyclerView.configureWith(devicesController, showDivider = true)
        viewModel.requestPasswordLiveData.observeEvent(this) {
            maybeShowDeleteDeviceWithPasswordDialog()
        }
    }

    override fun getCommonViewEvent(): DataSource<CommonViewEvents>? = viewModel.viewEvents

    override fun showFailure(throwable: Throwable) {
        super.showFailure(throwable)

        // Password is maybe not good, for safety measure, reset it here
        mAccountPassword = ""
    }

    override fun onDestroyView() {
        devicesController.callback = null
        recyclerView.cleanup()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()

        (activity as? VectorBaseActivity)?.supportActionBar?.setTitle(R.string.settings_devices_list)
    }

    override fun onDeviceClicked(deviceInfo: DeviceInfo) {
        viewModel.handle(DevicesAction.ToggleDevice(deviceInfo))
    }

    override fun onDeleteDevice(deviceInfo: DeviceInfo) {
        viewModel.handle(DevicesAction.Delete(deviceInfo))
    }

    override fun onRenameDevice(deviceInfo: DeviceInfo) {
        displayDeviceRenameDialog(deviceInfo)
    }

    override fun retry() {
        viewModel.handle(DevicesAction.Retry)
    }

    /**
     * Display an alert dialog to rename a device
     *
     * @param deviceInfo device info
     */
    private fun displayDeviceRenameDialog(deviceInfo: DeviceInfo) {
        val inflater = requireActivity().layoutInflater
        val layout = inflater.inflate(R.layout.dialog_base_edit_text, null)

        val input = layout.findViewById<EditText>(R.id.edit_text)
        input.setText(deviceInfo.displayName)

        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.devices_details_device_name)
                .setView(layout)
                .setPositiveButton(R.string.ok) { _, _ ->
                    val newName = input.text.toString()

                    viewModel.handle(DevicesAction.Rename(deviceInfo, newName))
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    /**
     * Show a dialog to ask for user password, or use a previously entered password.
     */
    private fun maybeShowDeleteDeviceWithPasswordDialog() {
        if (mAccountPassword.isNotEmpty()) {
            viewModel.handle(DevicesAction.Password(mAccountPassword))
        } else {
            val inflater = requireActivity().layoutInflater
            val layout = inflater.inflate(R.layout.dialog_device_delete, null)
            val passwordEditText = layout.findViewById<EditText>(R.id.delete_password)

            AlertDialog.Builder(requireActivity())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.devices_delete_dialog_title)
                    .setView(layout)
                    .setPositiveButton(R.string.devices_delete_submit_button_label, DialogInterface.OnClickListener { _, _ ->
                        if (passwordEditText.toString().isEmpty()) {
                            requireActivity().toast(R.string.error_empty_field_your_password)
                            return@OnClickListener
                        }
                        mAccountPassword = passwordEditText.text.toString()
                        viewModel.handle(DevicesAction.Password(mAccountPassword))
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
                        if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.cancel()
                            return@OnKeyListener true
                        }
                        false
                    })
                    .show()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        devicesController.update(state)

        handleRequestStatus(state.request)
    }

    private fun handleRequestStatus(unIgnoreRequest: Async<Unit>) {
        when (unIgnoreRequest) {
            is Loading -> waiting_view.isVisible = true
            else       -> waiting_view.isVisible = false
        }
    }
}
