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

package org.qubership.atp.environments.helper;

import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

@Component
public class JsonPathHandler {

    /**
     * Gets formatted json by jsonpath.
     *
     * @param json     - input json structure
     * @param jsonpath - jsonpath for getting part of input json structure
     * @return string in json format
     * @throws PathNotFoundException - exception during finding jsonpath in input json structure
     */
    public String getByJsonpath(String json, String jsonpath) throws PathNotFoundException {
        Object resultObj = JsonPath.read(json, jsonpath);
        if (resultObj instanceof String) {
            return resultObj.toString().replace("\"", "");
        } else {
            JsonElement element = new JsonParser().parse(resultObj.toString());
            return new GsonBuilder().setPrettyPrinting().create().toJson(element);
        }
    }
}
