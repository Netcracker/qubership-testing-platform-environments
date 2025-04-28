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

package org.qubership.atp.environments.model.utils;

import java.util.UUID;

public interface Constants {

    interface Project {

        UUID DEFAULT = UUID.fromString("f38dd8d0-fffc-465c-a369-81c3d15a316c");
    }

    interface Environment {

        interface Category {

            UUID ENVIRONMENT = UUID.fromString("ccaf6f9b-902a-42f7-bc11-3b1f0557919c");
            UUID TOOL = UUID.fromString("56cbd659-0912-4709-a4e0-cd2f9aafcf36");
            UUID TEMPORARY_ENVIRONMENT = UUID.fromString("884d7c4c-ba6a-4ec5-843a-4aa87225f7b4");
        }

        interface System {

            UUID DEFAULT = UUID.fromString("3bdef735-b163-4992-980b-b5e5ebcb77af");

            interface Connection {

                UUID HTTP = UUID.fromString("2a0eab16-0fe7-4a12-8155-78c0c151abdf");
                UUID SSH = UUID.fromString("24136d83-5ffb-487f-9bb4-e73be3a89aa2");
                UUID DB = UUID.fromString("46ca25d6-058e-471a-9b5e-c13e4b481227");
                UUID OPENSHIFT_SERVER = UUID.fromString("e4b8a9aa-1952-4be7-8687-56d95f078d32");
                UUID OPENSHIFT_SYSTEM = UUID.fromString("2cb3b9e0-0067-46af-8f18-b103fbc19a73");
                UUID KUBERNETES_PROJECT = UUID.fromString("657ecd97-b08c-46dd-bd87-6f574429c468");
                UUID TA_ENGINES_PROVIDER = UUID.fromString("a03d3884-36d5-40ad-947a-7e2c3f0febcb");
            }
        }
    }

    interface SystemCategories {
        UUID OPENSHIFT_SERVICE = UUID.fromString("643ecd97-b08c-46dd-bd87-6f574429c468");
        UUID KUBERNETES_SERVICE = UUID.fromString("efda918d-80aa-4ca7-99b8-d6864e7ce03e");
        UUID OPENSHIFT_SERVER = UUID.fromString("53792a2e-2da8-4686-a2a8-79b36ba33ed8");
        UUID KUBERNETES_SERVER = UUID.fromString("774845e8-42e0-4a99-86cb-3aa945c859aa");
        UUID ITF_LITE = UUID.fromString("4f623222-b5ab-4bf2-8cd4-77dda7693501");
    }
}
