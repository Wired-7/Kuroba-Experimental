package com.github.k1rakishou.chan.core.site.loader

sealed class ThreadLoadResult {
  class LoadedNormally(val chanLoaderResponse: ChanLoaderResponse) : ThreadLoadResult()
  class LoadedFromDatabaseCopy(val chanLoaderResponse: ChanLoaderResponse, val deleted: Boolean) : ThreadLoadResult()
}