package com.mokelab.sisyphus.feature.home.algorithm

import com.mokelab.sisyphus.core.database.entity.InputOutputType
import org.junit.Assert.*
import org.junit.Test

class RecommendationEngineTest {

    @Test
    fun `calculateDailyTimeBudget returns default when no data`() {
        val budget = RecommendationEngine.calculateDailyTimeBudget(
            weeklyStudyData = emptyList(),
            dayOfWeek = 1,
            taskHistory = emptyList()
        )
        assertEquals(60, budget)
    }

    @Test
    fun `calculateDailyTimeBudget uses median of same day data`() {
        val data = listOf(
            DailyStudyData(30, 1),  // Monday
            DailyStudyData(60, 1),
            DailyStudyData(90, 1),
            DailyStudyData(120, 2), // Tuesday (different day)
        )
        val budget = RecommendationEngine.calculateDailyTimeBudget(
            weeklyStudyData = data,
            dayOfWeek = 1,  // Monday
            taskHistory = emptyList()
        )
        // Median of [30, 60, 90] = 60, with default 0.7 factor = 42
        assertEquals(42, budget)
    }

    @Test
    fun `calculateDailyTimeBudget increases with high completion rate`() {
        val data = listOf(DailyStudyData(60, 1))
        val highCompletion = listOf(TaskCompletionData(true, 0.95f))

        val budget = RecommendationEngine.calculateDailyTimeBudget(
            weeklyStudyData = data,
            dayOfWeek = 1,
            taskHistory = highCompletion
        )
        // 60 * 1.2 = 72
        assertEquals(72, budget)
    }

    @Test
    fun `calculateDailyTimeBudget decreases with low completion rate`() {
        val data = listOf(DailyStudyData(60, 1))
        val lowCompletion = listOf(TaskCompletionData(true, 0.3f))

        val budget = RecommendationEngine.calculateDailyTimeBudget(
            weeklyStudyData = data,
            dayOfWeek = 1,
            taskHistory = lowCompletion
        )
        // 60 * 0.6 = 36
        assertEquals(36, budget)
    }

    @Test
    fun `calculateUrgency returns 1 for overdue cards`() {
        val urgency = RecommendationEngine.calculateUrgency(
            dueDue = System.currentTimeMillis() - 86400000L,  // 1 day overdue
            now = System.currentTimeMillis()
        )
        assertTrue("Urgency should be > 0", urgency > 0f)
        assertTrue("Urgency should be <= 1", urgency <= 1f)
    }

    @Test
    fun `calculateUrgency decreases over time`() {
        val now = System.currentTimeMillis()
        val recent = RecommendationEngine.calculateUrgency(now - 3600000L, now)  // 1 hour overdue
        val old = RecommendationEngine.calculateUrgency(now - 604800000L, now)  // 7 days overdue
        assertTrue("Recent should be more urgent than old", recent > old)
    }

    @Test
    fun `knapsackSelect fills within capacity`() {
        val items = listOf(
            RecommendationItem(RecommendationType.FSRS_REVIEW, 1, null, 20, 0.9f, InputOutputType.INPUT),
            RecommendationItem(RecommendationType.WEAK_SUBJECT, 2, null, 30, 0.5f, InputOutputType.INPUT),
            RecommendationItem(RecommendationType.EXERCISE, 3, null, 25, 0.6f, InputOutputType.OUTPUT)
        )
        val selected = RecommendationEngine.knapsackSelect(items, 50)
        val totalMinutes = selected.sumOf { it.estimatedMinutes }
        assertTrue("Total should be within capacity", totalMinutes <= 50)
    }

    @Test
    fun `knapsackSelect prefers high priority per minute`() {
        val items = listOf(
            RecommendationItem(RecommendationType.FSRS_REVIEW, 1, null, 10, 0.9f, InputOutputType.INPUT),
            RecommendationItem(RecommendationType.EXERCISE, 2, null, 50, 0.5f, InputOutputType.OUTPUT)
        )
        val selected = RecommendationEngine.knapsackSelect(items, 50)
        // First item has 0.9/10 = 0.09, second has 0.5/50 = 0.01
        assertTrue("High priority item should be selected first", selected.first().subjectId == 1L)
    }

    @Test
    fun `generateRecommendations returns at most 5 items`() {
        val result = RecommendationEngine.generateRecommendations(
            dailyBudget = 120,
            dueCards = (1..10).map { Triple(it.toLong(), System.currentTimeMillis() - 86400000L, 15) },
            subjectWeights = mapOf(1L to 5f, 2L to 3f, 3L to 7f)
        )
        assertTrue("Should return at most 5 items", result.size <= 5)
    }

    @Test
    fun `generateRecommendations includes due cards`() {
        val now = System.currentTimeMillis()
        val result = RecommendationEngine.generateRecommendations(
            dailyBudget = 120,
            dueCards = listOf(Triple(1L, now - 86400000L, 15)),
            subjectWeights = mapOf(1L to 5f)
        )
        assertTrue("Should include FSRS review", result.any { it.type == RecommendationType.FSRS_REVIEW })
    }

    @Test
    fun `checkInputOutputBalance returns NONE for balanced data`() {
        val records = listOf(
            Pair(InputOutputType.INPUT, "2026-01-01"),
            Pair(InputOutputType.OUTPUT, "2026-01-02"),
            Pair(InputOutputType.INPUT, "2026-01-03")
        )
        val warning = RecommendationEngine.checkInputOutputBalance(records)
        assertEquals(BalanceWarning.NONE, warning)
    }

    @Test
    fun `checkInputOutputBalance returns STRONG for 3 days pure input`() {
        val records = listOf(
            Pair(InputOutputType.INPUT, "2026-01-01"),
            Pair(InputOutputType.INPUT, "2026-01-02"),
            Pair(InputOutputType.INPUT, "2026-01-03")
        )
        val warning = RecommendationEngine.checkInputOutputBalance(records)
        assertEquals(BalanceWarning.STRONG, warning)
    }

    @Test
    fun `checkInputOutputBalance returns SOFT for 2 days pure input`() {
        val records = listOf(
            Pair(InputOutputType.INPUT, "2026-01-01"),
            Pair(InputOutputType.INPUT, "2026-01-02"),
            Pair(InputOutputType.OUTPUT, "2026-01-03")
        )
        val warning = RecommendationEngine.checkInputOutputBalance(records)
        assertEquals(BalanceWarning.SOFT, warning)
    }
}
