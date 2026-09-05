package com.shevay.oddlyspecific.challenge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ChallengeEngineTest {

    private ChallengeEngine challengeEngine;

    @BeforeEach
    void setUp() {
        challengeEngine = new ChallengeEngine();
    }

    @Test
    void testGetAllChallenges() {
        List<Challenge> challenges = challengeEngine.getAllChallenges();
        assertNotNull(challenges);
        assertEquals(6, challenges.size());
    }

    @Test
    void testGetRandomChallenge() {
        Challenge challenge = challengeEngine.getRandomChallenge();
        assertNotNull(challenge);
        assertNotNull(challenge.getId());
        assertNotNull(challenge.getTitle());
        assertNotNull(challenge.getInstructions());
        assertNotNull(challenge.getType());
    }

    @Test
    void testGetChallengeById() {
        Optional<Challenge> opt = challengeEngine.getChallengeById("REACTION_TEST");
        assertTrue(opt.isPresent());
        assertEquals("Neural Reflex Calibration", opt.get().getTitle());

        Optional<Challenge> invalid = challengeEngine.getChallengeById("NON_EXISTENT");
        assertTrue(invalid.isEmpty());
    }
}
