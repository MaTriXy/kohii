/*
 * Copyright (c) 2019 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kohii.core

import kohii.media.Media
import kohii.media.PlaybackInfo
import kohii.media.VolumeInfo
import kohii.v1.Bridge

abstract class Playable(
  val media: Media,
  internal val config: Config
) {

  data class Config(
    internal val tag: Any = Master.NO_TAG,
    internal val rendererType: Class<*>
  )

  abstract val tag: Any

  internal abstract val bridge: Bridge<*>

  internal abstract var playback: Playback?

  internal abstract var manager: PlayableManager?

  internal abstract val playerState: Int

  // TODO optimize this with VolumeInfo. Consider to split the 2.
  internal abstract var playbackInfo: PlaybackInfo

  abstract fun onPrepare(loadSource: Boolean)

  // Ensure the preparation for the playback
  abstract fun onReady()

  abstract fun onPlay()

  abstract fun onPause()

  abstract fun onReset()

  abstract fun onRelease()

  abstract fun onUnbind(playback: Playback)

  /**
   * Return `true` to indicate that this Playable would survive configuration changes and no
   * playback reloading would be required. In special cases like YouTube playback, it is recommended
   * to return `false` so Kohii will handle the resource recycling correctly.
   */
  abstract fun onConfigChange(): Boolean

  /**
   * Once the Playback finds it is good time for the Listener to request/release the Renderer, it
   * will trigger these calls to send that signal. The 'good time' can varies due to the actual
   * use case. In Kohii, there are 2 following cases:
   * - The Playback's Container is also the renderer. In this case, the Container/Renderer will
   * always be there. We suggest that the Listener should request for the Renderer as soon as
   * possible, and release it as late as possible. The proper place to do that are when the
   * Playback becomes active (onActive()) and inactive (onInActive()).
   *
   * @see [StaticViewRendererPlayback]
   *
   * - The Playback's Container is not the Renderer. In this case, the Renderer is expected
   * to be created on demand and release as early as possible, so that Kohii can reuse it for
   * other Playback as soon as possible. We suggest that the Listener should request for
   * the Renderer just right before the Playback starts (onPlay()), and release the Renderer
   * just right after the Playback pauses (onPause()).
   *
   * Flow:  If Bridge<RENDERER> needs a renderer
   *          ⬇
   *        Playable#considerRequestRenderer(playback)
   *          ⬇
   *        Manager#requestRenderer(playback, playable)
   *          ⬇
   *        Playback#attachRenderer(renderer)
   *          ⬇
   *        Playback#onAttachRenderer(renderer)
   *          ⬇
   *        If valid renderer returns, do the update for Bridge<RENDERER>
   *
   * @see [DynamicViewRendererPlayback]
   * @see [DynamicFragmentRendererPlayback]
   */
  abstract fun considerRequestRenderer(playback: Playback)

  /**
   * Once the Playback finds it is good time for the Listener to request/release the Renderer, it
   * will trigger these calls to send that signal. The 'good time' can varies due to the actual
   * use case. In Kohii, there are 2 following cases:
   * - The Playback's Container is also the renderer. In this case, the Container/Renderer will
   * always be there. We suggest that the Listener should request for the Renderer as soon as
   * possible, and release it as late as possible. The proper place to do that are when the
   * Playback becomes active (onActive()) and inactive (onInActive()).
   *
   * @see [StaticViewRendererPlayback]
   *
   * - The Playback's Container is not the Renderer. In this case, the Renderer is expected
   * to be created on demand and release as early as possible, so that Kohii can reuse it for
   * other Playback as soon as possible. We suggest that the Listener should request for
   * the Renderer just right before the Playback starts (onPlay()), and release the Renderer
   * just right after the Playback pauses (onPause()).
   *
   * Flow:  If Bridge<RENDERER> has a renderer to release
   *          ⬇
   *        Update the renderer in Bridge<RENDERER>
   *          ⬇
   *        Manager#releaseRenderer(playback, playable)
   *          ⬇
   *        Playback#detachRenderer(renderer)
   *          ⬇
   *        Playback#onDetachRenderer(renderer)
   *          ⬇
   *        If the renderer is managed by pool, it will now be released back to the pool for reuse.
   *
   * @see [DynamicViewRendererPlayback]
   * @see [DynamicFragmentRendererPlayback]
   */
  abstract fun considerReleaseRenderer(playback: Playback)

  internal abstract fun onDistanceChanged(
    playback: Playback,
    from: Int,
    to: Int
  )

  internal abstract fun onVolumeInfoChange(
    playback: Playback,
    from: VolumeInfo,
    to: VolumeInfo
  )
}
