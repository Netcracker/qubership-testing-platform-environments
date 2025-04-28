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

package org.qubership.atp.environments.version.regexp;

public class TimeoutRegexCharSequence implements CharSequence {
    private final CharSequence inner;

    private final int timeoutMillis;

    private final long timeoutTime;


    /**
     * Constructor for TimeoutRegexCharSequence.
     * @param inner regexp pattern.
     * @param timeoutMillis timeout (ms) for regexp processing.
     */
    public TimeoutRegexCharSequence(CharSequence inner, int timeoutMillis) {
        super();
        this.inner = inner;
        this.timeoutMillis = timeoutMillis;
        timeoutTime = System.currentTimeMillis() + timeoutMillis;
    }

    /**
     * Method for regexp processing to char with timeout check.
     */
    public char charAt(int index) {
        if (System.currentTimeMillis() > timeoutTime) {
            String message = String
                    .format("Timeout occurred after %s ms while processing regular expression %s.", timeoutMillis,
                            inner);
            throw new RuntimeException(message);
        }
        return inner.charAt(index);
    }

    public int length() {
        return inner.length();
    }

    public CharSequence subSequence(int start, int end) {
        return new TimeoutRegexCharSequence(inner.subSequence(start, end), timeoutMillis);
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}
