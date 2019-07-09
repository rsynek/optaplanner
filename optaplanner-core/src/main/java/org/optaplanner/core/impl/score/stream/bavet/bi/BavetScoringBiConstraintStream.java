/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.score.stream.bavet.bi;

import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.impl.score.inliner.BigDecimalWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.IntWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.LongWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;
import org.optaplanner.core.impl.score.inliner.UndoScoreImpacter;
import org.optaplanner.core.impl.score.inliner.WeightedScoreImpacter;
import org.optaplanner.core.impl.score.stream.bavet.BavetConstraint;
import org.optaplanner.core.impl.score.stream.bavet.common.BavetNodeBuildPolicy;

public final class BavetScoringBiConstraintStream<Solution_, A, B> extends BavetAbstractBiConstraintStream<Solution_, A, B> {

    private final boolean positive;
    private final boolean noMatchWeigher;
    private final ToIntBiFunction<A, B> intMatchWeigher;
    private final ToLongBiFunction<A, B> longMatchWeigher;
    private final BiFunction<A, B, BigDecimal> bigDecimalMatchWeigher;

    public BavetScoringBiConstraintStream(BavetConstraint<Solution_> bavetConstraint, boolean positive) {
        this(bavetConstraint, positive, true, null, null, null);
    }

    public BavetScoringBiConstraintStream(BavetConstraint<Solution_> bavetConstraint, boolean positive, ToIntBiFunction<A, B> intMatchWeigher) {
        this(bavetConstraint, positive, false, intMatchWeigher, null, null);
        if (intMatchWeigher == null) {
            throw new IllegalArgumentException("The matchWeigher (null) cannot be null.");
        }
    }

    public BavetScoringBiConstraintStream(BavetConstraint<Solution_> bavetConstraint, boolean positive, ToLongBiFunction<A, B> longMatchWeigher) {
        this(bavetConstraint, positive, false, null, longMatchWeigher, null);
        if (longMatchWeigher == null) {
            throw new IllegalArgumentException("The matchWeigher (null) cannot be null.");
        }
    }

    public BavetScoringBiConstraintStream(BavetConstraint<Solution_> bavetConstraint, boolean positive, BiFunction<A, B, BigDecimal> bigDecimalMatchWeigher) {
        this(bavetConstraint, positive, false, null, null, bigDecimalMatchWeigher);
        if (bigDecimalMatchWeigher == null) {
            throw new IllegalArgumentException("The matchWeigher (null) cannot be null.");
        }
    }

    private BavetScoringBiConstraintStream(BavetConstraint<Solution_> bavetConstraint,
            boolean positive, boolean noMatchWeigher,
            ToIntBiFunction<A, B> intMatchWeigher, ToLongBiFunction<A, B> longMatchWeigher, BiFunction<A, B, BigDecimal> bigDecimalMatchWeigher) {
        super(bavetConstraint);
        this.positive = positive;
        this.noMatchWeigher = noMatchWeigher;
        this.intMatchWeigher = intMatchWeigher;
        this.longMatchWeigher = longMatchWeigher;
        this.bigDecimalMatchWeigher = bigDecimalMatchWeigher;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    protected void assertChildStreamListSize() {
        if (!childStreamList.isEmpty()) {
            throw new IllegalStateException("Impossible state: the stream (" + this
                    + ") has an non-empty childStreamList (" + childStreamList + ") but it's an endpoint.");
        }
    }

    @Override
    protected BavetScoringBiNode<A, B> createNode(BavetNodeBuildPolicy<Solution_> buildPolicy,
            Score<?> constraintWeight, int nodeOrder, BavetAbstractBiNode<A, B> parentNode) {
        if (!positive) {
            constraintWeight = constraintWeight.negate();
        }
        ScoreInliner scoreInliner = buildPolicy.getSession().getScoreInliner();
        WeightedScoreImpacter weightedScoreImpacter = scoreInliner.buildWeightedScoreImpacter(constraintWeight);
        TriFunction<A, B, Consumer<Score<?>>, UndoScoreImpacter> scoreImpacter;
        if (weightedScoreImpacter instanceof IntWeightedScoreImpacter) {
            IntWeightedScoreImpacter castedWeightedScoreImpacter = (IntWeightedScoreImpacter) weightedScoreImpacter;
            if (intMatchWeigher != null) {
                scoreImpacter = (A a, B b, Consumer<Score<?>> matchScoreConsumer) -> {
                    int matchWeight = intMatchWeigher.applyAsInt(a, b);
                    return castedWeightedScoreImpacter.impactScore(matchWeight, matchScoreConsumer);
                };
            } else if (noMatchWeigher) {
                scoreImpacter = (A a, B b, Consumer<Score<?>> matchScoreConsumer) ->
                        castedWeightedScoreImpacter.impactScore(1, matchScoreConsumer);
            } else {
                throw new IllegalStateException("The matchWeigher of " + BiConstraintStream.class.getSimpleName()
                        + ".penalize(matchWeigher) of the constraint (" + constraint.getConstraintId()
                        + ") must return an int.");
            }
        } else if (weightedScoreImpacter instanceof LongWeightedScoreImpacter) {
            LongWeightedScoreImpacter castedWeightedScoreImpacter = (LongWeightedScoreImpacter) weightedScoreImpacter;
            if (longMatchWeigher != null) {
                scoreImpacter = (A a, B b, Consumer<Score<?>> matchScoreConsumer) -> {
                    long matchWeight = longMatchWeigher.applyAsLong(a, b);
                    return castedWeightedScoreImpacter.impactScore(matchWeight, matchScoreConsumer);
                };
            } else if (noMatchWeigher) {
                scoreImpacter = (A a, B b, Consumer<Score<?>> matchScoreConsumer) ->
                        castedWeightedScoreImpacter.impactScore(1L, matchScoreConsumer);
            } else {
                throw new IllegalStateException("The matchWeigher of " + BiConstraintStream.class.getSimpleName()
                        + ".penalize(matchWeigher) of the constraint (" + constraint.getConstraintId()
                        + ") must return a long.");
            }
        } else if (weightedScoreImpacter instanceof BigDecimalWeightedScoreImpacter) {
            BigDecimalWeightedScoreImpacter castedWeightedScoreImpacter = (BigDecimalWeightedScoreImpacter) weightedScoreImpacter;
            if (bigDecimalMatchWeigher != null) {
                scoreImpacter = (A a, B b, Consumer<Score<?>> matchScoreConsumer) -> {
                    BigDecimal matchWeight = bigDecimalMatchWeigher.apply(a, b);
                    return castedWeightedScoreImpacter.impactScore(matchWeight, matchScoreConsumer);
                };
            } else if (noMatchWeigher) {
                scoreImpacter = (A a, B b, Consumer<Score<?>> matchScoreConsumer) ->
                        castedWeightedScoreImpacter.impactScore(BigDecimal.ONE, matchScoreConsumer);
            } else {
                throw new IllegalStateException("The matchWeigher of " + BiConstraintStream.class.getSimpleName()
                        + ".penalize(matchWeigher) of the constraint (" + constraint.getConstraintId()
                        + ") must return a " + BigDecimal.class.getSimpleName() + ".");
            }
        } else {
            throw new IllegalStateException("Unsupported weightedScoreImpacter (" + weightedScoreImpacter + ").");
        }
        BavetScoringBiNode<A, B> node = new BavetScoringBiNode<>(buildPolicy.getSession(), nodeOrder,
                constraint.getConstraintPackage(), constraint.getConstraintName(),
                constraintWeight, scoreImpacter);
        buildPolicy.addScoringNode(node);
        return node;
    }

    @Override
    public String toString() {
        return "Scoring()";
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

}
