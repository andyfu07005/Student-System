package com.sims.service;

import com.sims.entity.GpaAlgorithm;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GradePointCalculatorTest {

    private final GradePointCalculator calculator = new GradePointCalculator();

    @Test
    void convertsScoresToFourPointGradePoints() {
        assertEquals(new BigDecimal("4.0"), calculator.toGradePoint(new BigDecimal("95"), GpaAlgorithm.FOUR_POINT));
        assertEquals(new BigDecimal("3.7"), calculator.toGradePoint(new BigDecimal("88"), GpaAlgorithm.FOUR_POINT));
        assertEquals(new BigDecimal("2.3"), calculator.toGradePoint(new BigDecimal("74"), GpaAlgorithm.FOUR_POINT));
        assertEquals(new BigDecimal("0.0"), calculator.toGradePoint(new BigDecimal("59"), GpaAlgorithm.FOUR_POINT));
    }

    @Test
    void convertsScoresToFivePointGradePoints() {
        assertEquals(new BigDecimal("5.0"), calculator.toGradePoint(new BigDecimal("95"), GpaAlgorithm.FIVE_POINT));
        assertEquals(new BigDecimal("4.0"), calculator.toGradePoint(new BigDecimal("84"), GpaAlgorithm.FIVE_POINT));
        assertEquals(new BigDecimal("2.0"), calculator.toGradePoint(new BigDecimal("68"), GpaAlgorithm.FIVE_POINT));
        assertEquals(new BigDecimal("0.0"), calculator.toGradePoint(new BigDecimal("59"), GpaAlgorithm.FIVE_POINT));
    }

    @Test
    void calculatesWeightedGpaByCredits() {
        BigDecimal weightedPoints = new BigDecimal("4.0").multiply(new BigDecimal("3.0"))
                .add(new BigDecimal("3.0").multiply(new BigDecimal("2.0")));

        BigDecimal gpa = calculator.weightedGpa(weightedPoints, new BigDecimal("5.0"));

        assertEquals(new BigDecimal("3.60"), gpa);
    }
}
