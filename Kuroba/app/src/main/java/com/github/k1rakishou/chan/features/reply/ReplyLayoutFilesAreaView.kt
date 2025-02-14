package com.github.k1rakishou.chan.features.reply

import com.github.k1rakishou.chan.ui.helper.picker.AbstractFilePicker

interface ReplyLayoutFilesAreaView {
  fun showFilePickerErrorToast(filePickerError: AbstractFilePicker.FilePickerError)
  fun showGenericErrorToast(errorMessage: String)
  fun requestReplyLayoutWrappingModeUpdate()
  fun showLoadingView(cancellationFunc: () -> Unit, titleTextId: Int)
  fun hideLoadingView()
  fun updateSendButtonState(selectedFilesCount: Int, maxAllowedSelectedFilesCount: Int)
  fun updateSelectedFilesCounter(selectedCount: Int, maxAllowedCount: Int, totalCount: Int)
  fun showFileStatusMessage(fileStatusString: String)
  fun showReplyLayoutMessage(message: String?, hideDelayMs: Int)

  fun onDontKeepActivitiesSettingDetected()
}