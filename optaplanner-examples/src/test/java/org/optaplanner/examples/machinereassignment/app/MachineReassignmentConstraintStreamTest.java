/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.machinereassignment.app;

import java.io.File;

import org.junit.Test;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.machinereassignment.domain.MachineReassignment;

public class MachineReassignmentConstraintStreamTest extends SolverPerformanceTest<MachineReassignment> {

    public MachineReassignmentConstraintStreamTest(String moveThreadCount) {
        super(moveThreadCount);
    }

    @Override
    protected CommonApp<MachineReassignment> createCommonApp() {
        return new MachineReassignmentApp();
    }

    // 18:41:47.369 [limited test] INFO  Solving ended: time spent (1264), best score (0hard/-44324766soft),
    // score calculation speed (321330/sec), phase total (2), environment mode (FAST_ASSERT)
    @Test(timeout = 600000)
    public void solveModel_a2_1() {
        File unsolvedDataFile = new File("data/machinereassignment/unsolved/model_a2_1.xml");
        runSpeedTest(unsolvedDataFile, 10, EnvironmentMode.FULL_ASSERT);
    }
}
