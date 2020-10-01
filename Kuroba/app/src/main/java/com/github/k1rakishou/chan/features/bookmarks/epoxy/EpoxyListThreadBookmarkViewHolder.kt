package com.github.k1rakishou.chan.features.bookmarks.epoxy

import android.content.Context
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.github.k1rakishou.chan.Chan
import com.github.k1rakishou.chan.R
import com.github.k1rakishou.chan.features.bookmarks.data.ThreadBookmarkStats
import com.github.k1rakishou.chan.ui.theme.ThemeEngine
import com.github.k1rakishou.model.data.descriptor.ChanDescriptor
import javax.inject.Inject

@EpoxyModelClass(layout = R.layout.epoxy_list_thread_bookmark_view)
abstract class EpoxyListThreadBookmarkViewHolder
  : EpoxyModelWithHolder<BaseThreadBookmarkViewHolder>(), ThemeEngine.ThemeChangesListener {

  @Inject
  lateinit var themeEngine: ThemeEngine

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var imageLoaderRequestData: BaseThreadBookmarkViewHolder.ImageLoaderRequestData? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var threadDescriptor: ChanDescriptor.ThreadDescriptor? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var bookmarkClickListener: ((ChanDescriptor.ThreadDescriptor) -> Unit)? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var bookmarkStatsClickListener: ((ChanDescriptor.ThreadDescriptor) -> Unit)? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var context: Context? = null

  @EpoxyAttribute
  var threadBookmarkStats: ThreadBookmarkStats? = null
  @EpoxyAttribute
  var titleString: String? = null
  @EpoxyAttribute
  var highlightBookmark: Boolean = false

  private var holder: BaseThreadBookmarkViewHolder? = null

  override fun bind(holder: BaseThreadBookmarkViewHolder) {
    super.bind(holder)
    Chan.inject(this)

    this.holder = holder

    holder.setImageLoaderRequestData(imageLoaderRequestData)
    holder.setDescriptor(threadDescriptor)
    holder.setThreadBookmarkStats(false, threadBookmarkStats)
    holder.bookmarkClickListener(bookmarkClickListener)
    holder.bookmarkStatsClickListener(false, bookmarkStatsClickListener)
    holder.setTitle(titleString)
    holder.highlightBookmark(highlightBookmark)

    val watching = threadBookmarkStats?.watching ?: true
    context?.let { holder.bindImage(false, watching, it) }

    themeEngine.addListener(this)
  }

  override fun unbind(holder: BaseThreadBookmarkViewHolder) {
    super.unbind(holder)

    themeEngine.removeListener(this)
    holder.unbind()

    this.holder = null
  }

  override fun onThemeChanged() {
    holder?.apply {
      setThreadBookmarkStats(true, threadBookmarkStats)
      setTitle(titleString)
      highlightBookmark(highlightBookmark)
    }
  }

  override fun createNewHolder(): BaseThreadBookmarkViewHolder {
    return BaseThreadBookmarkViewHolder(
      context!!.resources.getDimension(R.dimen.thread_list_bookmark_view_image_size).toInt()
    )
  }

}