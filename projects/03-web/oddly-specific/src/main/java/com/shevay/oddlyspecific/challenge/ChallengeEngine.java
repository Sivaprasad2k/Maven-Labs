package com.shevay.oddlyspecific.challenge;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ChallengeEngine {

    private final Map<String, Challenge> challengeRegistry = new LinkedHashMap<>();

    public ChallengeEngine() {
        initChallenges();
    }

    private void initChallenges() {
        // 1. Reaction Test
        challengeRegistry.put("REACTION_TEST", new Challenge(
                "REACTION_TEST",
                "Neural Reflex Calibration",
                "Wait for the indicator to flash GREEN, then click the target as fast as possible!",
                "REACTION",
                Map.of("delayMinMs", 1500, "delayMaxMs", 4000)
        ));

        // 2. Memory Sequence
        challengeRegistry.put("MEMORY_SEQUENCE", new Challenge(
                "MEMORY_SEQUENCE",
                "Quantum Memory Matrix",
                "Observe the 4-color pattern and repeat the exact sequence.",
                "MEMORY",
                Map.of("sequenceLength", 4, "colors", List.of("cyan", "magenta", "yellow", "lime"))
        ));

        // 3. Don't Click Challenge
        challengeRegistry.put("DONT_CLICK", new Challenge(
                "DONT_CLICK",
                "Temporal Impulse Resistance",
                "DO NOT CLICK THE RED BUTTON. Wait for the 5-second countdown to complete.",
                "TIMED_WAIT",
                Map.of("countdownSeconds", 5)
        ));

        // 4. Moving Button
        challengeRegistry.put("MOVING_BUTTON", new Challenge(
                "MOVING_BUTTON",
                "Evasive Target Interception",
                "Catch and click the evasive button 4 times as it dodges your cursor.",
                "EVASIVE",
                Map.of("requiredClicks", 4)
        ));

        // 5. Absurd Human Verification
        challengeRegistry.put("HUMAN_VERIFICATION", new Challenge(
                "HUMAN_VERIFICATION",
                "Absurd Human Verification",
                "Adjust the precision calibration slider to exactly 42.7% to verify human consciousness.",
                "SLIDER",
                Map.of("targetValue", 42.7, "tolerance", 1.5)
        ));

        // 6. Number Challenge
        challengeRegistry.put("NUMBER_CHALLENGE", new Challenge(
                "NUMBER_CHALLENGE",
                "Ascending Numerical Order",
                "Click the scrambled numbers 1 through 5 in strictly ascending order.",
                "SEQUENCE",
                Map.of("count", 5)
        ));
    }

    public Challenge getRandomChallenge() {
        List<Challenge> list = new ArrayList<>(challengeRegistry.values());
        int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(randomIndex);
    }

    public Optional<Challenge> getChallengeById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(challengeRegistry.get(id));
    }

    public List<Challenge> getAllChallenges() {
        return new ArrayList<>(challengeRegistry.values());
    }
}
