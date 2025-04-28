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

package org.qubership.atp.environments.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

public class ReaderTest {

    private final ThreadLocal<InputStream> is = new ThreadLocal<>();
    private final ThreadLocal<InputStream> err = new ThreadLocal<>();

    @BeforeEach
    public void setUp() {
        is.set(Mockito.mock(InputStream.class));
        err.set(Mockito.mock(InputStream.class));
    }

    @Test
    public void readInputTest_returnOptional() throws IOException, TimeoutException {
        Assertions.assertFalse(Reader.readInput(() -> true, is.get(), err.get(), Collectors.joining()).isPresent());
    }

    @Test
    public void readInputTest_catchIllegalArgumentException() {
        try(MockedConstruction<Reader> ignored = Mockito.mockConstruction(Reader.class,
                (mock, context) -> when(mock.flush()).thenReturn(Optional.of("ERROR")))) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> Reader.readInput(
                    () -> true, is.get(), err.get(), Collectors.joining()));
        }

    }

    @Test
    public void readInputTest_catchTimeOutException() {
        Assertions.assertThrows(TimeoutException.class, () -> Reader.readInput(
                () -> false, is.get(), err.get(), Collectors.joining()));
    }

    @Test
    public void readAttempt_ReturnFalse() throws IOException {
        Reader reader = new Reader(is.get(), Collectors.counting());
        when(is.get().available()).thenReturn(0);
        Assertions.assertFalse(reader.readAttempt());
    }

    @Test
    public void fileReadAttempt_CatchInterruptedIOException() throws IOException {
        Reader reader = new Reader(is.get(), Collectors.counting());
        when(is.get().read(any(), anyInt(), anyInt())).thenReturn(-1);
        Assertions.assertThrows(InterruptedIOException.class, reader::fileReadAttempt);
    }

    @Test
    public void fileReadAttempt_Successful() throws IOException {
        Reader reader = new Reader(is.get(), Collectors.counting());
        when(is.get().read(any(), anyInt(), anyInt())).thenReturn(0);
        Assertions.assertTrue(reader.fileReadAttempt());
    }
}
