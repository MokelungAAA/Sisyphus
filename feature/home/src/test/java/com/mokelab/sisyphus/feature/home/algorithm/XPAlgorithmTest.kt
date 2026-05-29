package com.mokelab.sisyphus.feature.home.algorithm

import com.mokelab.sisyphus.core.database.entity.InputOutputType
import org.junit.Assert.*
import org.junit.Test

class XPAlgorithmTest {

    @Test
    fun `calculateXP returns positive value`() {
        val xp = XPAlgorithm.calculateXP(
            durationMinutes = 30,
            inputType = InputOutputType.INPUT,
            subjectWeight = 5f,
            userLevel = 1
        )
        assertTrue("XP should be positive", xp > 0f)
    }

    @Test
    fun `calculateXP OUTPUT is higher than INPUT`() {
        val inputXP = XPAlgorithm.calculateXP(
            durationMinutes = 30,
            inputType = InputOutputType.INPUT,
            subjectWeight = 5f,
            userLevel = 1
        )
        val outputXP = XPAlgorithm.calculateXP(
            durationMinutes = 30,
            inputType = InputOutputType.OUTPUT,
            subjectWeight = 5f,
            userLevel = 1
        )
        assertTrue("OUTPUT XP should be higher than INPUT", outputXP > inputXP)
    }

    @Test
    fun `calculateXP increases with level`() {
        val lowLevelXP = XPAlgorithm.calculateXP(
            durationMinutes = 30,
            inputType = InputOutputType.INPUT,
            subjectWeight = 5f,
            userLevel = 1
        )
        val highLevelXP = XPAlgorithm.calculateXP(
            durationMinutes = 30,
            inputType = InputOutputType.INPUT,
            subjectWeight = 5f,
            userLevel = 50
        )
        assertTrue("Higher level should give more XP", highLevelXP > lowLevelXP)
    }

    @Test
    fun `calculateXP increases with duration`() {
        val shortXP = XPAlgorithm.calculateXP(
            durationMinutes = 10,
            inputType = InputOutputType.INPUT,
            subjectWeight = 5f,
            userLevel = 1
        )
        val longXP = XPAlgorithm.calculateXP(
            durationMinutes = 60,
            inputType = InputOutputType.INPUT,
            subjectWeight = 5f,
            userLevel = 1
        )
        assertTrue("Longer study should give more XP", longXP > shortXP)
    }

    @Test
    fun `fitXPLevel returns value between 0 and 1`() {
        val result = XPAlgorithm.fitXPLevel(
            examScoreRate = 0.7f,
            dailyStudyMinutes = 60f,
            fsrsRetention = 0.85f,
            taskCompletionRate = 0.6f
        )
        assertTrue("Fit result should be >= 0", result >= 0f)
        assertTrue("Fit result should be <= 1", result <= 1f)
    }

    @Test
    fun `fitXPLevel weights exam score most heavily`() {
        val highExam = XPAlgorithm.fitXPLevel(
            examScoreRate = 0.9f,
            dailyStudyMinutes = 30f,
            fsrsRetention = 0.5f,
            taskCompletionRate = 0.5f
        )
        val lowExam = XPAlgorithm.fitXPLevel(
            examScoreRate = 0.3f,
            dailyStudyMinutes = 120f,
            fsrsRetention = 0.95f,
            taskCompletionRate = 0.95f
        )
        assertTrue("High exam score should result in higher fit", highExam > lowExam)
    }

    @Test
    fun `getTargetScoreRate returns correct values`() {
        assertEquals(0.50f, XPAlgorithm.getTargetScoreRate(40), 0.01f)
        assertEquals(0.60f, XPAlgorithm.getTargetScoreRate(50), 0.01f)
        assertEquals(0.70f, XPAlgorithm.getTargetScoreRate(60), 0.01f)
        assertEquals(0.95f, XPAlgorithm.getTargetScoreRate(100), 0.01f)
    }

    @Test
    fun `getTargetScoreRate interpolates between levels`() {
        val rate45 = XPAlgorithm.getTargetScoreRate(45)
        assertTrue("Level 45 should be between 50% and 60%", rate45 > 0.50f && rate45 < 0.60f)
    }

    @Test
    fun `getTitle returns correct titles`() {
        assertEquals("初学者", XPAlgorithm.getTitle(1))
        assertEquals("学徒", XPAlgorithm.getTitle(10))
        assertEquals("探索者", XPAlgorithm.getTitle(20))
        assertEquals("传奇", XPAlgorithm.getTitle(100))
    }

    @Test
    fun `requiresExam at multiples of 10`() {
        assertTrue("Level 10 should require exam", XPAlgorithm.requiresExam(10))
        assertTrue("Level 20 should require exam", XPAlgorithm.requiresExam(20))
        assertFalse("Level 5 should not require exam", XPAlgorithm.requiresExam(5))
        assertFalse("Level 0 should not require exam", XPAlgorithm.requiresExam(0))
    }

    @Test
    fun `adjustXPCurve returns different multipliers based on gap`() {
        val behindConfig = XPAlgorithm.adjustXPCurve(50, 0.6f, 0.4f)  // gap = 0.2
        val aheadConfig = XPAlgorithm.adjustXPCurve(50, 0.6f, 0.8f)   // gap = -0.2
        val normalConfig = XPAlgorithm.adjustXPCurve(50, 0.6f, 0.6f)  // gap = 0

        assertEquals(1.2f, behindConfig.levelMultiplier)
        assertEquals(1.8f, aheadConfig.levelMultiplier)
        assertEquals(1.5f, normalConfig.levelMultiplier)
    }

    @Test
    fun `xpForLevel increases with level`() {
        val xp10 = XPAlgorithm.xpForLevel(10)
        val xp50 = XPAlgorithm.xpForLevel(50)
        assertTrue("Higher level should require more XP", xp50 > xp10)
    }
}
