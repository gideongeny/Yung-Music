package com.gideongeng.music.innertube.pages

import com.gideongeng.music.innertube.models.Album
import com.gideongeng.music.innertube.models.AlbumItem
import com.gideongeng.music.innertube.models.Artist
import com.gideongeng.music.innertube.models.ArtistItem
import com.gideongeng.music.innertube.models.MusicResponsiveListItemRenderer
import com.gideongeng.music.innertube.models.MusicTwoRowItemRenderer
import com.gideongeng.music.innertube.models.PlaylistItem
import com.gideongeng.music.innertube.models.SongItem
import com.gideongeng.music.innertube.models.YTItem
import com.gideongeng.music.innertube.models.oddElements
import com.gideongeng.music.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            return AlbumItem(
                        browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
                        playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchPlaylistEndpoint?.playlistId ?: return null,
                        title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                        artists = null,
                        year = renderer.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                        thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                        explicit = renderer.subtitleBadges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null
                    )
        }
    }
}
