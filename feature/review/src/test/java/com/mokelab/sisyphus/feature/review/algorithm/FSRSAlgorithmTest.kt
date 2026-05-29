package com.mokelab.sisyphus.feature.review.algorithm

import com.mokelab.sisyphus.core.database.entity.CardState
import org.junit.Assert.*
import org.junit.Test

class FSRSAlgorithmTest {

    @Test
    fun `calculateInitialStability returns positive value`() {
        val params = FSRSParams()
        for (rating in Rating.entries) {
            val stability = FSRSAlgorithm.calculateInitialStability(rating, params)
            assertTrue("Initial stability for $rating should be > 0", stability > 0f)
        }
    }

    @Test
    fun `calculateInitialStability EASY is greater than GOOD`() {
        val params = FSRSParams()
        val easy = FSRSAlgorithm.calculateInitialStability(Rating.EASY, params)
        val good = FSRSAlgorithm.calculateInitialStability(Rating.GOOD, params)
        assertTrue("EASY stability should be > GOOD", easy > good)
    }

    @Test
    fun `calculateInitialDifficulty is in range 0 to 1`() {
        val params = FSRSParams()
        for (rating in Rating.entries) {
            val difficulty = FSRSAlgorithm.calculateInitialDifficulty(rating, params)
            assertTrue("Difficulty for $rating should be >= 0", difficulty >= 0f)
            assertTrue("Difficulty for $rating should be <= 1", difficulty <= 1f)
        }
    }

    @Test
    fun `calculateDifficulty clamps to 0 to 1`() {
        val params = FSRSParams()
        // Start from high difficulty, use EASY (negative delta)
        val result = FSRSAlgorithm.calculateDifficulty(0.9f, Rating.EASY, params)
        assertTrue("Difficulty should be >= 0", result >= 0f)
        assertTrue("Difficulty should be <= 1", result <= 1f)
    }

    @Test
    fun `calculateStability for existing card returns greater stability`() {
        val params = FSRSParams()
        val stability = FSRSAlgorithm.calculateStability(
            stability = 5f,
            difficulty = 0.5f,
            elapsedDays = 1,
            rating = Rating.GOOD,
            params = params
        )
        assertTrue("Stability should increase on GOOD rating", stability > 5f)
    }

    @Test
    fun `calculateInterval returns positive value`() {
        val params = FSRSParams()
        val interval = FSRSAlgorithm.calculateInterval(10f, params)
        assertTrue("Interval should be > 0", interval > 0)
        assertTrue("Interval should be <= maxInterval", interval <= params.maximumInterval)
    }

    @Test
    fun `calculateInterval clamps to maximumInterval`() {
        val params = FSRSParams()
        val interval = FSRSAlgorithm.calculateInterval(100000f, params)
        assertEquals("Interval should be clamped to max", params.maximumInterval, interval)
    }

    @Test
    fun `determineState NEW with GOOD goes to LEARNING`() {
        val state = FSRSAlgorithm.determineState(CardState.NEW, Rating.GOOD)
        assertEquals(CardState.LEARNING, state)
    }

    @Test
    fun `determineState NEW with EASY goes to REVIEW`() {
        val state = FSRSAlgorithm.determineState(CardState.NEW, Rating.EASY)
        assertEquals(CardState.REVIEW, state)
    }

    @Test
    fun `determineState REVIEW with AGAIN goes to RELEARNING`() {
        val state = FSRSAlgorithm.determineState(CardState.REVIEW, Rating.AGAIN)
        assertEquals(CardState.RELEARNING, state)
    }

    @Test
    fun `determineState LEARNING with GOOD goes to REVIEW`() {
        val state = FSRSAlgorithm.determineState(CardState.LEARNING, Rating.GOOD)
        assertEquals(CardState.REVIEW, state)
    }

    @Test
    fun `calculateRetrievability returns value between 0 and 1`() {
        val r = FSRSAlgorithm.calculateRetrievability(10f, 5)
        assertTrue("Retrievability should be >= 0", r >= 0f)
        assertTrue("Retrievability should be <= 1", r <= 1f)
    }

    @Test
    fun `calculateRetrievability decreases over time`() {
        val r1 = FSRSAlgorithm.calculateRetrievability(10f, 1)
        val r2 = FSRSAlgorithm.calculateRetrievability(10f, 10)
        assertTrue("Retrievability should decrease over time", r1 > r2)
    }

    @Test
    fun `nextReview creates valid card from NEW state`() {
        val card = FSRSAlgorithm.createNewCard(knowledgePointId = 1L)
        val params = FSRSParams()
        val now = System.currentTimeMillis()

        val updated = FSRSAlgorithm.nextReview(card, Rating.GOOD, params, now)

        assertTrue("Stability should be > 0", updated.stability > 0f)
        assertTrue("Reps should be 1", updated.reps == 1)
        assertTrue("Scheduled days should be > 0", updated.scheduledDays > 0)
        assertNotEquals("State should not be NEW", CardState.NEW, updated.state)
    }

    @Test
    fun `nextReview AGAIN increments lapses`() {
        val card = FSRSAlgorithm.createNewCard(knowledgePointId = 1L)
        val params = FSRSParams()
        val now = System.currentTimeMillis()

        val updated = FSRSAlgorithm.nextReview(card, Rating.AGAIN, params, now)

        assertEquals("Lapses should be 1", 1, updated.lapses)
    }

    @Test
    fun `createNewCard has NEW state and zero values`() {
        val card = FSRSAlgorithm.createNewCard(knowledgePointId = 42L)

        assertEquals(CardState.NEW, card.state)
        assertEquals(0f, card.stability)
        assertEquals(0f, card.difficulty)
        assertEquals(0, card.reps)
        assertEquals(0, card.lapses)
        assertEquals(42L, card.knowledgePointId)
    }
}
