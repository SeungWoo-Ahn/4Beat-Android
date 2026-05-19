package com.fourbeat.domain.usecase.music

import com.fourbeat.domain.model.post.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ResolveBestMatchSongUseCaseTest {

    private lateinit var useCase: ResolveBestMatchSongUseCase

    @Before
    fun setUp() {
        useCase = ResolveBestMatchSongUseCase()
    }

    private fun printScores(title: String, artist: String, candidates: List<Song>) {
        println("\n=== Target: \"$title\" / \"$artist\" ===")
        useCase.scoreAll(title, artist, candidates)
            .sortedByDescending { it.second }
            .forEach { (song, score) ->
                println("  ${"%.3f".format(score)} | ${song.title} - ${song.artist}")
            }
    }

    @Test
    fun `정확히 일치하는 곡이 선택된다`() {
        val candidates = listOf(
            Song("Love Story", "Taylor Swift", "url1"),
            Song("Love Story", "Indila", "url2"),
            Song("Love Me Like You Do", "Ellie Goulding", "url3"),
        )
        printScores("Love Story", "Taylor Swift", candidates)
        val result = useCase("Love Story", "Taylor Swift", candidates)
        assertEquals("Taylor Swift", result?.artist)
    }

    @Test
    fun `피처링 아티스트 포함 시 매칭된다`() {
        val candidates = listOf(
            Song("Dynamite", "BTS feat. Halsey", "url1"),
            Song("Dynamite", "Taio Cruz", "url2"),
        )
        printScores("Dynamite", "BTS", candidates)
        val result = useCase("Dynamite", "BTS", candidates)
        assertEquals("BTS feat. Halsey", result?.artist)
    }

    @Test
    fun `괄호 제목 변형에도 매칭된다`() {
        // 괄호 버전만 후보 — sanitize 후 "밤편지"로 인식되는지 검증
        val candidates = listOf(
            Song("밤편지 (8월의 크리스마스 OST)", "IU", "url1"),
            Song("Hype Boy", "NewJeans", "url2"),
        )
        printScores("밤편지", "IU", candidates)
        val result = useCase("밤편지", "IU", candidates)
        assertEquals("IU", result?.artist)
    }

    @Test
    fun `Inst 버전보다 원곡이 먼저 있으면 원곡이 선택된다`() {
        // Inst. 패널티 없음 — 동점 시 Spotify 결과 순서(원곡 우선)에 의존
        val candidates = listOf(
            Song("밤편지", "IU", "url1"),
            Song("밤편지 (Inst.)", "IU", "url2"),
        )
        printScores("밤편지", "IU", candidates)
        val result = useCase("밤편지", "IU", candidates)
        assertEquals("밤편지", result?.title)
    }

    @Test
    fun `대괄호 제목 변형에도 매칭된다`() {
        // result.title은 원본 Song 그대로 반환 — 괄호 제거는 점수 계산에만 적용됨
        val candidates = listOf(
            Song("Blinding Lights [Official MV]", "The Weeknd", "url1"),
            Song("Save Your Tears", "The Weeknd", "url2"),
        )
        printScores("Blinding Lights", "The Weeknd", candidates)
        val result = useCase("Blinding Lights", "The Weeknd", candidates)
        assertEquals("The Weeknd", result?.artist)
        assertEquals("Blinding Lights [Official MV]", result?.title)
    }

    @Test
    fun `완전히 다른 곡이면 null 반환`() {
        val candidates = listOf(
            Song("Shape of You", "Ed Sheeran", "url1"),
            Song("Blinding Lights", "The Weeknd", "url2"),
        )
        printScores("밤편지", "IU", candidates)
        val result = useCase("밤편지", "IU", candidates)
        assertNull(result)
    }

    @Test
    fun `Timeless Instrumental SG 워너비 실제 후보에서 SG Wannabe 선택`() {
        val candidates = listOf(
            Song("Timeless - Instrumental", "The Weeknd", "url1"),
            Song("Timeless - Instrumental", "Dxvid", "url2"),
            Song("Timeless - Instrumental", "NovaX", "url3"),
            Song("Timeless - Instrumental", "The Weeknd", "url4"),
            Song("Timeless - Instrumental", "BIBI", "url5"),
            Song("Timeless - Instrumental Version", "Ann Paris", "url6"),
            Song("TIMELESS - Instrumental", "GyeongseoYeji", "url7"),
            Song("Timeless", "SG Wannabe", "url8"),
            Song("TIMELESS", "GyeongseoYeji", "url9"),
            Song("Timeless (feat. TRADE L, Street Baby)", "TOIL", "url10"),
        )
        printScores("Timeless - Instrumental", "SG 워너비", candidates)
        val result = useCase("Timeless - Instrumental", "SG 워너비", candidates)
        println("선택된 곡: ${result?.title} / ${result?.artist}")
        assertEquals("SG Wannabe", result?.artist)
    }

    @Test
    fun `후보가 없으면 null 반환`() {
        val result = useCase("밤편지", "IU", emptyList())
        assertNull(result)
    }

    @Test
    fun `동명이곡에서 아티스트로 구분된다`() {
        val candidates = listOf(
            Song("Hype Boy", "NewJeans", "url1"),
            Song("Hype Boy", "Unknown Artist", "url2"),
        )
        printScores("Hype Boy", "NewJeans", candidates)
        val result = useCase("Hype Boy", "NewJeans", candidates)
        assertEquals("NewJeans", result?.artist)
    }
}
