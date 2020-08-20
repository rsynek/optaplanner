/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.heuristic.selector.move.generic;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collections;
import java.util.List;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.pillar.PillarSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.PillarChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.entity.pillar.PillarSelector;
import org.optaplanner.core.impl.heuristic.selector.entity.pillar.PillarSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.AbstractMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelectorFactory;

public class PillarChangeMoveSelectorFactory extends AbstractMoveSelectorFactory<PillarChangeMoveSelectorConfig> {

    public PillarChangeMoveSelectorFactory(PillarChangeMoveSelectorConfig moveSelectorConfig) {
        super(moveSelectorConfig);
    }

    @Override
    public MoveSelector buildBaseMoveSelector(HeuristicConfigPolicy configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        PillarSelectorConfig pillarSelectorConfig_ =
                defaultIfNull(config.getPillarSelectorConfig(), new PillarSelectorConfig());
        List<String> variableNameIncludeList = config.getValueSelectorConfig() == null
                || config.getValueSelectorConfig().getVariableName() == null ? null
                        : Collections.singletonList(config.getValueSelectorConfig().getVariableName());
        PillarSelector pillarSelector = PillarSelectorFactory.create(pillarSelectorConfig_).buildPillarSelector(configPolicy,
                config.getSubPillarType(), config.getSubPillarSequenceComparatorClass(),
                minimumCacheType, SelectionOrder.fromRandomSelectionBoolean(randomSelection), variableNameIncludeList);
        ValueSelectorConfig valueSelectorConfig_ =
                defaultIfNull(config.getValueSelectorConfig(), new ValueSelectorConfig());
        SelectionOrder selectionOrder = SelectionOrder.fromRandomSelectionBoolean(randomSelection);
        ValueSelector valueSelector = ValueSelectorFactory.create(valueSelectorConfig_).buildValueSelector(configPolicy,
                pillarSelector.getEntityDescriptor(), minimumCacheType, selectionOrder);
        return new PillarChangeMoveSelector(pillarSelector, valueSelector, randomSelection);
    }
}