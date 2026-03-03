package org.thingsboard.common.util;

import net.objecthunter.exp4j.Expression;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ExpressionUtilsTest {

    private static final double EPSILON = 1e-9;

    @Test
    void givenSimpleConstantExpression_whenEvaluated_thenReturnsCorrectResult() {
        Expression expr = ExpressionUtils.createExpression("2 + 3", Set.of());
        assertThat(expr.evaluate()).isEqualTo(5.0);
    }

    @Test
    void givenVariableExpression_whenVariableSet_thenEvaluatesCorrectly() {
        Expression expr = ExpressionUtils.createExpression("x * 2", Set.of("x"));
        expr.setVariable("x", 7.0);
        assertThat(expr.evaluate()).isEqualTo(14.0);
    }

    @Test
    void givenMultiVariableExpression_whenBothSet_thenEvaluatesCorrectly() {
        Expression expr = ExpressionUtils.createExpression("a + b", Set.of("a", "b"));
        expr.setVariable("a", 10.0);
        expr.setVariable("b", 5.0);
        assertThat(expr.evaluate()).isEqualTo(15.0);
    }

    @Test
    void givenLnFunction_whenApplied_thenReturnsNaturalLog() {
        Expression expr = ExpressionUtils.createExpression("ln(x)", Set.of("x"));
        expr.setVariable("x", Math.E);
        assertThat(expr.evaluate()).isCloseTo(1.0, within(EPSILON));
    }

    @Test
    void givenLgFunction_whenApplied_thenReturnsLog10() {
        Expression expr = ExpressionUtils.createExpression("lg(x)", Set.of("x"));
        expr.setVariable("x", 100.0);
        assertThat(expr.evaluate()).isCloseTo(2.0, within(EPSILON));
    }

    @Test
    void givenLogabFunction_whenApplied_thenReturnsLogBaseA() {
        // logab(base, value) = log(value) / log(base)
        Expression expr = ExpressionUtils.createExpression("logab(2, 8)", Set.of());
        assertThat(expr.evaluate()).isCloseTo(3.0, within(EPSILON));
    }

    @Test
    void givenSqrtFunction_whenApplied_thenReturnsCorrectResult() {
        Expression expr = ExpressionUtils.createExpression("sqrt(x)", Set.of("x"));
        expr.setVariable("x", 9.0);
        assertThat(expr.evaluate()).isCloseTo(3.0, within(EPSILON));
    }

    @Test
    void givenAbsFunction_whenAppliedToNegative_thenReturnsPositive() {
        Expression expr = ExpressionUtils.createExpression("abs(x)", Set.of("x"));
        expr.setVariable("x", -5.0);
        assertThat(expr.evaluate()).isCloseTo(5.0, within(EPSILON));
    }

    @Test
    void givenSinFunction_whenApplied_thenReturnsCorrectResult() {
        Expression expr = ExpressionUtils.createExpression("sin(x)", Set.of("x"));
        expr.setVariable("x", 0.0);
        assertThat(expr.evaluate()).isCloseTo(0.0, within(EPSILON));
    }

    @Test
    void givenCosFunction_whenApplied_thenReturnsCorrectResult() {
        Expression expr = ExpressionUtils.createExpression("cos(x)", Set.of("x"));
        expr.setVariable("x", 0.0);
        assertThat(expr.evaluate()).isCloseTo(1.0, within(EPSILON));
    }

    @Test
    void givenPowFunction_whenApplied_thenReturnsCorrectResult() {
        Expression expr = ExpressionUtils.createExpression("pow(2, 10)", Set.of());
        assertThat(expr.evaluate()).isCloseTo(1024.0, within(EPSILON));
    }

    @Test
    void givenFloorFunction_whenApplied_thenReturnsFractionalFloor() {
        Expression expr = ExpressionUtils.createExpression("floor(x)", Set.of("x"));
        expr.setVariable("x", 3.9);
        assertThat(expr.evaluate()).isCloseTo(3.0, within(EPSILON));
    }

    @Test
    void givenCeilFunction_whenApplied_thenReturnsFractionalCeiling() {
        Expression expr = ExpressionUtils.createExpression("ceil(x)", Set.of("x"));
        expr.setVariable("x", 3.1);
        assertThat(expr.evaluate()).isCloseTo(4.0, within(EPSILON));
    }

    @Test
    void givenImplicitMultiplication_whenUsed_thenEvaluatesCorrectly() {
        Expression expr = ExpressionUtils.createExpression("2x", Set.of("x"));
        expr.setVariable("x", 5.0);
        assertThat(expr.evaluate()).isCloseTo(10.0, within(EPSILON));
    }

    @Test
    void givenUserDefinedFunctions_thenListIsNotEmpty() {
        assertThat(ExpressionUtils.userDefinedFunctions).isNotEmpty();
        assertThat(ExpressionUtils.userDefinedFunctions.stream()
                .map(net.objecthunter.exp4j.function.Function::getName))
                .contains("ln", "lg", "logab");
    }
}