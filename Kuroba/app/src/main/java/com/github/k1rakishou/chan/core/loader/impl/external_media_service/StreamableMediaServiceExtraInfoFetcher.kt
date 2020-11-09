package com.github.k1rakishou.chan.core.loader.impl.external_media_service

import android.graphics.BitmapFactory
import com.github.k1rakishou.ChanSettings
import com.github.k1rakishou.chan.R
import com.github.k1rakishou.chan.core.loader.impl.post_comment.ExtraLinkInfo
import com.github.k1rakishou.chan.core.loader.impl.post_comment.LinkInfoRequest
import com.github.k1rakishou.chan.core.loader.impl.post_comment.SpanUpdateBatch
import com.github.k1rakishou.chan.utils.AppModuleAndroidUtils
import com.github.k1rakishou.chan.utils.BackgroundUtils
import com.github.k1rakishou.common.AndroidUtils
import com.github.k1rakishou.common.ModularResult
import com.github.k1rakishou.common.groupOrNull
import com.github.k1rakishou.model.data.media.GenericVideoId
import com.github.k1rakishou.model.data.video_service.MediaServiceType
import com.github.k1rakishou.model.repository.MediaServiceLinkExtraContentRepository
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import java.util.regex.Pattern

internal class StreamableMediaServiceExtraInfoFetcher(
  private val mediaServiceLinkExtraContentRepository: MediaServiceLinkExtraContentRepository
) : ExternalMediaServiceExtraInfoFetcher() {

  override val mediaServiceType: MediaServiceType
    get() = MediaServiceType.Streamable

  override fun isEnabled(): Boolean {
    return AppModuleAndroidUtils.shouldLoadForNetworkType(
      ChanSettings.parseStreamableTitlesAndDuration.get()
    )
  }

  override fun isCached(videoId: GenericVideoId): Single<Boolean> {
    BackgroundUtils.ensureBackgroundThread()

    return rxSingle {
      return@rxSingle mediaServiceLinkExtraContentRepository.isCached(videoId, mediaServiceType)
        .unwrap()
    }
  }

  override fun fetch(
    requestUrl: String,
    linkInfoRequest: LinkInfoRequest
  ): Single<ModularResult<SpanUpdateBatch>> {
    BackgroundUtils.ensureBackgroundThread()

    return rxSingle {
      if (!isEnabled()) {
        return@rxSingle ModularResult.value(
          SpanUpdateBatch(
            requestUrl,
            ExtraLinkInfo.Success(mediaServiceType, null, null),
            linkInfoRequest.oldPostLinkableSpans,
            streamableIcon
          )
        )
      }

      return@rxSingle genericFetch(
        TAG,
        streamableIcon,
        requestUrl,
        linkInfoRequest,
        mediaServiceLinkExtraContentRepository
      )
    }
  }

  override fun linkMatchesToService(link: String): Boolean {
    return streamableLinkPattern.matcher(link).matches()
  }

  override fun extractLinkVideoId(link: String): GenericVideoId {
    val matcher = streamableLinkPattern.matcher(link)
    if (!matcher.matches()) {
      throw IllegalStateException("Couldn't match link ($link) with the current service. " +
        "Did you forget to call linkMatchesToService() first?")
    }

    val videoId = checkNotNull(matcher.groupOrNull(1)) {
      "Couldn't extract videoId out of the input link ($link)"
    }

    return GenericVideoId(videoId)
  }

  override fun formatRequestUrl(link: String): String {
    return link
  }

  companion object {
    private const val TAG = "StreamableMediaServiceExtraInfoFetcher"

    private val streamableLinkPattern = Pattern.compile("https:\\/\\/streamable\\.com\\/([a-zA-Z0-9]+)")
    private val streamableIcon = BitmapFactory.decodeResource(AndroidUtils.getRes(), R.drawable.streamable_icon)
  }

}