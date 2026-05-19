package com.fourbeat.domain.usecase.music

import com.fourbeat.domain.model.post.Song
import javax.inject.Inject
import kotlin.math.max

class ResolveBestMatchSongUseCase @Inject constructor() {
    operator fun invoke(
        targetTitle: String,
        targetArtist: String,
        candidates: List<Song>
    ): Song? {
        if (candidates.isEmpty()) return null

        val normTargetTitle = sanitizeMetadata(targetTitle)
        val normTargetArtist = sanitizeMetadata(targetArtist)

        return candidates
            .map { song ->
                val score = calculateMatchScore(
                    normTargetTitle,
                    normTargetArtist,
                    sanitizeMetadata(song.title),
                    sanitizeMetadata(song.artist),
                )
                song to score
            }
            .filter { it.second > 0.4 }
            .maxByOrNull { it.second }
            ?.first
    }

    internal fun scoreAll(
        targetTitle: String,
        targetArtist: String,
        candidates: List<Song>,
    ): List<Pair<Song, Double>> {
        val normTitle = sanitizeMetadata(targetTitle)
        val normArtist = sanitizeMetadata(targetArtist)
        return candidates.map { song ->
            song to calculateMatchScore(
                normTitle,
                normArtist,
                sanitizeMetadata(song.title),
                sanitizeMetadata(song.artist),
            )
        }
    }

    private fun calculateMatchScore(
        targetTitle: String,
        targetArtist: String,
        candidateTitle: String,
        candidateArtist: String,
    ): Double {
        val titleScore = when {
            candidateTitle.contains(targetTitle) || targetTitle.contains(candidateTitle) ->
                max(0.7, getSimilarity(targetTitle, candidateTitle))
            else -> getSimilarity(targetTitle, candidateTitle)
        }

        val artistScore = when {
            candidateArtist.contains(targetArtist) || targetArtist.contains(candidateArtist) ->
                max(0.8, getSimilarity(targetArtist, candidateArtist))
            commonPrefixLength(targetArtist, candidateArtist) >= 2 ->
                max(0.5, getSimilarity(targetArtist, candidateArtist))
            else -> getSimilarity(targetArtist, candidateArtist)
        }

        return (titleScore + artistScore) / 2.0
    }

    private fun commonPrefixLength(s1: String, s2: String): Int =
        s1.zip(s2).takeWhile { (a, b) -> a == b }.count()

    /**
     * 비교를 위한 텍스트 정규화 (소문자화, 괄호 제거, 특수문자 제거)
     */
    private fun sanitizeMetadata(text: String): String {
        return text.lowercase()
            .replace("[(\\[].*?[)\\]]".toRegex(), "")
            .replace("[^a-zA-Z0-9가-힣]".toRegex(), "")
            .trim()
    }

    /**
     * 레벤슈타인 거리 기반 유사도 산출 (0.0 ~ 1.0)
     */
    private fun getSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val distance = computeLevenshteinDistance(s1, s2)
        val maxLen = max(s1.length, s2.length)
        return 1.0 - (distance.toDouble() / maxLen)
    }

    /**
     * 레벤슈타인 거리 알고리즘 (편집 거리 계산)
     */
    private fun computeLevenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // 삭제
                    dp[i][j - 1] + 1,      // 삽입
                    dp[i - 1][j - 1] + cost // 수정
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}