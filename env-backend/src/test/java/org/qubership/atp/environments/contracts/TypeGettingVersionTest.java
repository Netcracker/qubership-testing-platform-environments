/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.environments.contracts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.model.utils.enums.TypeGettingVersion;

public class TypeGettingVersionTest {

    @Test
    public void checkTypeGettingVersionUpdated() {
        Assertions.assertEquals(8, TypeGettingVersion.values().length,
                "This is a friendly reminder that you have updated the enum TypeGettingVersion. "
                        + "Don't forget to also update the models in the openapi specifications "
                        + "and after that specify the new actual value for this test");
    }
}
