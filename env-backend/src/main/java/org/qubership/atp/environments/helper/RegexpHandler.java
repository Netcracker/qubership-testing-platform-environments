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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.qubership.atp.environments.version.regexp.TimeoutRegexCharSequence;
import org.springframework.stereotype.Component;

@Component
public class RegexpHandler {

    /**
     * Gets all matches by regexp.
     *
     * @param input  - input string
     * @param regexp - regexp for getting all matches in the input string
     * @return string with each match on a new line
     */
    public String getByRegExp(String input, String regexp, int timeout) {
        Pattern pattern = Pattern.compile(regexp, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(new TimeoutRegexCharSequence(input, timeout));
        List<String> listMatches = new ArrayList<>();
        while (matcher.find()) {
            listMatches.add(matcher.group());
        }
        return listMatches.stream().collect(Collectors.joining("\n", "", ""));
    }
}
