/*
 * KurobaEx - *chan browser https://github.com/K1rakishou/Kuroba-Experimental/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.k1rakishou.chan.ui.cell

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.k1rakishou.chan.R
import com.github.k1rakishou.chan.core.base.RendezvousCoroutineExecutor
import com.github.k1rakishou.chan.core.manager.BoardManager
import com.github.k1rakishou.chan.core.manager.ChanThreadManager
import com.github.k1rakishou.chan.core.site.sites.chan4.Chan4PagesRequest.BoardPage
import com.github.k1rakishou.chan.utils.AppModuleAndroidUtils
import com.github.k1rakishou.common.AndroidUtils
import com.github.k1rakishou.model.data.board.ChanBoard
import com.github.k1rakishou.model.data.descriptor.ChanDescriptor
import com.github.k1rakishou.model.data.descriptor.PostDescriptor
import com.github.k1rakishou.model.data.post.ChanOriginalPost
import com.github.k1rakishou.model.data.thread.ChanThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
class ThreadStatusCell(
  context: Context,
  attrs: AttributeSet
) : LinearLayout(context, attrs), View.OnClickListener {

  @Inject
  lateinit var themeEngine: com.github.k1rakishou.core_themes.ThemeEngine
  @Inject
  lateinit var boardManager: BoardManager
  @Inject
  lateinit var chanThreadManager: ChanThreadManager

  private lateinit var statusCellText: TextView

  private var callback: Callback? = null
  private var error: String? = null

  private val job = SupervisorJob()
  private val scope = CoroutineScope(job + Dispatchers.Main)
  private val rendezvousCoroutineExecutor = RendezvousCoroutineExecutor(scope)

  private var updateJob: Job? = null

  init {
    AppModuleAndroidUtils.extractStartActivityComponent(context)
      .inject(this)

    setBackgroundResource(R.drawable.item_background)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    schedule()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    unschedule()
  }

  override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
    super.onWindowFocusChanged(hasWindowFocus)

    if (hasWindowFocus) {
      schedule()
    } else {
      unschedule()
    }
  }

  private fun schedule() {
    unschedule()

    updateJob = scope.launch {
      delay(UPDATE_INTERVAL_MS)
      update()
    }
  }

  private fun unschedule() {
    updateJob?.cancel()
    updateJob = null
  }

  override fun onClick(v: View) {
    error = null

    if (callback?.getCurrentChanDescriptor() != null) {
      callback?.onListStatusClicked()
    }

    update()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    statusCellText = findViewById(R.id.text)
    statusCellText.typeface = themeEngine.chanTheme.mainFont
    statusCellText.setTextColor(themeEngine.chanTheme.textColorSecondary)

    setOnClickListener(this)
  }

  fun setCallback(callback: Callback?) {
    this.callback = callback
  }

  fun setError(error: String?) {
    this.error = error

    if (error == null) {
      schedule()
    }
  }

  fun update() {
    rendezvousCoroutineExecutor.post {
      updateInternal()
    }
  }

  @SuppressLint("SetTextI18n")
  private suspend fun updateInternal() {
    val chanDescriptor = callback?.getCurrentChanDescriptor()
      ?: return

    if (chanDescriptor is ChanDescriptor.CatalogDescriptor) {
      if (isClickable) {
        isClickable = false
      }

      if (isFocusable) {
        isFocusable = false
      }

      return
    }

    if (error != null) {
      statusCellText.text = "$error\n${AndroidUtils.getString(R.string.thread_refresh_bar_inactive)}"
      return
    }

    chanDescriptor as ChanDescriptor.ThreadDescriptor

    val chanThread = chanThreadManager.getChanThread(chanDescriptor)
      ?: return

    val canUpdate = chanThread.canUpdateThread()
    val builder = SpannableStringBuilder()

    if (appendThreadStatusPart(chanThread, builder)) {
      builder.append('\n')
    }

    appendThreadRefreshPart(chanThread, builder)
    builder.append('\n')

    val op = chanThread.getOriginalPost()
    val boardDescriptor = op.postDescriptor.boardDescriptor()

    if (boardDescriptor != null) {
      val board = boardManager.byBoardDescriptor(boardDescriptor)
      board?.let { appendThreadStatisticsPart(chanThread, builder, op, it) }
    }

    statusCellText.text = builder

    if (canUpdate) {
      schedule()
    }
  }

  private fun appendThreadStatisticsPart(
    chanThread: ChanThread,
    builder: SpannableStringBuilder,
    op: ChanOriginalPost,
    board: ChanBoard
  ) {
    val hasReplies = op.catalogRepliesCount >= 0 || chanThread.repliesCount > 0
    val hasImages = op.catalogImagesCount >= 0 || chanThread.imagesCount > 0

    if (hasReplies && hasImages) {
      val hasBumpLimit = board.bumpLimit > 0
      val hasImageLimit = board.imageLimit > 0

      val totalRepliesCount = if (op.catalogRepliesCount >= 0) {
        op.catalogRepliesCount
      } else {
        chanThread.postsCount - 1
      }

      val replies = SpannableString(totalRepliesCount.toString() + "R")
      if (hasBumpLimit && op.catalogRepliesCount >= board.bumpLimit) {
        replies.setSpan(StyleSpan(Typeface.ITALIC), 0, replies.length, 0)
      }

      val threadImagesCount = if (op.catalogImagesCount >= 0) {
        op.catalogImagesCount
      } else {
        chanThread.imagesCount
      }

      val images = SpannableString(threadImagesCount.toString() + "I")
      if (hasImageLimit && op.catalogImagesCount >= board.imageLimit) {
        images.setSpan(StyleSpan(Typeface.ITALIC), 0, images.length, 0)
      }

      builder
        .append(replies)
        .append(" / ")
        .append(images)

      if (op.uniqueIps >= 0) {
        val ips = op.uniqueIps.toString() + "P"
        builder.append(" / ").append(ips)
      }
    }

    val boardPage = callback?.getPage(op.postDescriptor)
    if (boardPage != null) {
      val page = SpannableString(boardPage.currentPage.toString())
      if (boardPage.currentPage >= board.pages) {
        page.setSpan(StyleSpan(Typeface.ITALIC), 0, page.length, 0)
      }

      builder
        .append(" / ")
        .append(AndroidUtils.getString(R.string.thread_page_no))
        .append(' ')
        .append(page)
    }
  }

  private suspend fun appendThreadRefreshPart(chanThread: ChanThread, builder: SpannableStringBuilder) {
    if (!chanThread.canUpdateThread()) {
      return
    }

    val timeSeconds = callback?.timeUntilLoadMoreMs()?.div(1000L)
      ?: return

    when {
      callback?.isWatching() == false -> {
        builder.append(AndroidUtils.getString(R.string.thread_refresh_bar_inactive))
      }
      timeSeconds <= 0 -> {
        builder.append(AndroidUtils.getString(R.string.loading))
      }
      else -> {
        builder.append(AndroidUtils.getString(R.string.thread_refresh_countdown, timeSeconds))
      }
    }
  }

  private fun appendThreadStatusPart(
    chanThread: ChanThread,
    builder: SpannableStringBuilder
  ): Boolean {
    when {
      chanThread.isArchived() -> {
        builder.append(AndroidUtils.getString(R.string.thread_archived))
        return true
      }
      chanThread.isClosed() -> {
        builder.append(AndroidUtils.getString(R.string.thread_closed))
        return true
      }
      chanThread.isDeleted() -> {
        builder.append(AndroidUtils.getString(R.string.thread_deleted))
        return true
      }
    }

    return false
  }

  interface Callback {
    suspend fun timeUntilLoadMoreMs(): Long
    fun isWatching(): Boolean
    fun getCurrentChanDescriptor(): ChanDescriptor?
    fun getPage(originalPostDescriptor: PostDescriptor): BoardPage?
    fun onListStatusClicked()
  }

  companion object {
    private const val UPDATE_INTERVAL_MS = 1000L
  }

}