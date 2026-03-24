/*
 * # Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.atp.environments.service.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.helper.JsonPathHandler;

import com.jayway.jsonpath.InvalidJsonException;

public class JsonPathHandlerTest {

    JsonPathHandler jsonPathHandler = new JsonPathHandler();
    private final String inputJsonString = """
            {
              "version": {
                "version": "v0.50.0-rc5",
                "param1": "unknown"
              },
              "version2": "v1.50.1-rc6",
              "param2": "4324"
            }\
            """;
    private final String inputWrongJsonString = """
            {
              "version": {
                "version": "v0.50.0-rc5",
                "param1": "unknown",
              "version2": "v1.50.1-rc6",
              "param2": "4324"
            }""";

    @Test
    public void getByJsonpath_specifiedKey_gotSimpleString() {
        String jsonpath = "$.version.version";
        String expectedSimpleString = "v0.50.0-rc5";
        Assertions.assertEquals(expectedSimpleString, jsonPathHandler.getByJsonpath(inputJsonString, jsonpath));
    }

    @Test
    public void getByJsonpath_recursiveDescentOperator_gotJsonArray() {
        String jsonpath = "$..version";
        String expectedComplexJson = """
                [
                  {
                    "version": "v0.50.0-rc5",
                    "param1": "unknown"
                  },
                  "v0.50.0-rc5"
                ]""";
        Assertions.assertEquals(expectedComplexJson, jsonPathHandler.getByJsonpath(inputJsonString, jsonpath));
    }

    @Test
    public void getByJsonpath_wrongJsonStructure_gotException() {
        String jsonpath = "$.version.version";
        Assertions.assertThrows(InvalidJsonException.class, () -> jsonPathHandler.getByJsonpath(inputWrongJsonString, jsonpath));
    }
}
