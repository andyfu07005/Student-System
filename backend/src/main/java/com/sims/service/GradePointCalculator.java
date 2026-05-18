package com.sims.service;

import com.sims.entity.GpaAlgorithm;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class GradePointCalculator {

    public BigDecimal toGradePoint(BigDecimal score, GpaAlgorithm algorithm) {
        if (algorithm == GpaAlgorithm.FIVE_POINT) {
            return toFivePoint(score);
        }
        return toFourPoint(score);
    }

    public BigDecimal weightedGpa(BigDecimal weightedPoints, BigDecimal credits) {
        if (credits == null || credits.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("0.00");
        }
        return weightedPoints.divide(credits, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal toFourPoint(BigDecimal score) {
        int value = score.intValue();
        if (value >= 90) return new BigDecimal("4.0");
        if (value >= 85) return new BigDecimal("3.7");
        if (value >= 82) return new BigDecimal("3.3");
        if (value >= 78) return new BigDecimal("3.0");
        if (value >= 75) return new BigDecimal("2.7");
        if (value >= 72) return new BigDecimal("2.3");
        if (value >= 68) return new BigDecimal("2.0");
        if (value >= 64) return new BigDecimal("1.5");
        if (value >= 60) return new BigDecimal("1.0");
        return new BigDecimal("0.0");
    }

    private BigDecimal toFivePoint(BigDecimal score) {
        int value = score.intValue();
        if (value >= 90) return new BigDecimal("5.0");
        if (value >= 80) return new BigDecimal("4.0");
        if (value >= 70) return new BigDecimal("3.0");
        if (value >= 60) return new BigDecimal("2.0");
        return new BigDecimal("0.0");
    }
}
