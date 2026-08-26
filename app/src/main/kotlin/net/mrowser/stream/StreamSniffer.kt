package net.mrowser.stream

import android.webkit.CookieManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * Collects media candidates for the current page (safe to call off the UI thread)
 * and assembles a PlaybackRequest. Pure selection lives in MediaUrlClassifier /
 * StreamCandidateSelector.
 */
class StreamSniffer(
    private val userAgent: () -> String,
    private val onStreamAvailable: () -> Unit,
    private val onCleared: () -> Unit
) {
    private val candidates = CopyOnWriteArrayList<StreamCandidate>()
    private val seq = AtomicInteger(0)

    @Volatile private var pageUrl: String = ""

    // onRequest runs on WebView worker threads; ConcurrentHashMap's add() is atomic,
    // so it doubles as both "have we already recorded this exact URL" (dedup) and
    // "claim the right to react to it" (announce) with no separate lock or race window.
    private val knownUrls = ConcurrentHashMap.newKeySet<String>()

    fun onPageStarted(url: String) {
        pageUrl = url
        candidates.clear()
        knownUrls.clear()
        onCleared()
    }

    fun onRequest(url: String) {
        val kind = MediaUrlClassifier.classify(url)
        if (kind != MediaUrlClassifier.MediaKind.MANIFEST_HLS && kind != MediaUrlClassifier.MediaKind.SUBTITLE) {
            return
        }
        // Skip URLs we've already recorded - some players re-request the same
        // manifest repeatedly (live-style refresh), and we only want a fresh
        // candidate / re-announcement for genuinely new URLs.
        if (!knownUrls.add(url)) return

        candidates.add(StreamCandidate(url, kind, seq.incrementAndGet()))

        if (kind == MediaUrlClassifier.MediaKind.MANIFEST_HLS) {
            // Fire every time a *new* manifest shows up, not just the first one on
            // the page - this is what makes the play chip re-light after the user
            // scrolls and the page lazy-loads another video.
            onStreamAvailable()
        }
    }

    fun hasStream(): Boolean =
        candidates.any { it.kind == MediaUrlClassifier.MediaKind.MANIFEST_HLS }

    fun bestRequest(): PlaybackRequest? {
        val best = StreamCandidateSelector.selectBest(candidates) ?: return null
        return buildRequest(best)
    }

    fun getAllVideoCandidates(): List<StreamCandidate> {
        return candidates
            .filter { it.kind == MediaUrlClassifier.MediaKind.MANIFEST_HLS && !MediaUrlClassifier.isAdHost(it.url) }
            .distinctBy { it.url }
    }

    fun buildRequest(candidate: StreamCandidate): PlaybackRequest {
        val headers = buildMap {
            put("User-Agent", userAgent())
            if (pageUrl.isNotEmpty()) put("Referer", pageUrl)
            CookieManager.getInstance().getCookie(candidate.url)?.let { put("Cookie", it) }
        }
        val subtitles = SubtitlePlan.build(StreamCandidateSelector.selectSubtitles(candidates))
        return PlaybackRequest(candidate.url, headers, subtitles, pageUrl)
    }
}
