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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.BooleanUtils;

import com.jcraft.jsch.Channel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Reader<R, A> implements AutoCloseable {

    public static final String NO_RESPONSE_FROM_REMOTE_SERVER = "No response from remote server";
    private static final int readAttempts = 600;
    private final InputStream is;
    private final byte[] buffer = new byte[1024];
    private final Collector<? super String, A, R> collector;
    private A collection;

    public Reader(InputStream is, Collector<? super String, A, R> collector) {
        this.is = is;
        this.collector = collector;
    }

    public static <R> Reader<R, ?> create(InputStream is, Collector<? super String, ?, R> collector) {
        return new Reader<>(is, collector);
    }

    /**
     * TODO Make javadoc documentation for this method.
     *
     * @param shouldStop when {@link Channel#isClosed()}.
     * @param is         {@link Channel#getInputStream()}.
     * @param err        {@link com.jcraft.jsch.ChannelExec#getErrStream()}.
     * @param collector  result collector.
     * @return contents of {@code InputStream is}.
     * @throws IOException              if an error occurred during reading the stream.
     * @throws IllegalArgumentException if error stream contains an error.
     */
    public static <R> Optional<R> readInput(Supplier<Boolean> shouldStop, InputStream is, InputStream err,
                                            Collector<? super String, ?, R> collector) throws IOException,
            TimeoutException {
        Reader<R, ?> contentReader = Reader.create(is, collector);
        Reader<String, ?> errorReader = Reader.create(err, Collectors.joining());
        AtomicInteger counter = new AtomicInteger(readAttempts);
        while (true) {
            if (!errorReader.readAttempt() && !contentReader.readAttempt()) {
                //nothing to read now
                if (BooleanUtils.isTrue(shouldStop.get())) {
                    break;//no more data
                }
                if (counter.getAndDecrement() == 0) { //nothing read and can't get exit signal in console
                    throw new TimeoutException(NO_RESPONSE_FROM_REMOTE_SERVER);
                }
                //will wait for more data
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error("Thead was interrupted: ", e);
                }
            } else {
                counter.set(readAttempts);
            }
        }
        errorReader.flush().ifPresent(message -> {
            //log.error(message);
            throw new IllegalArgumentException(message);
        });
        return contentReader.flush();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            //log.warn("Can not close resource: " + closeable, e);
        }
    }

    /**
     * Reads some chunk of data from the {@link InputStream}.
     *
     * @return true if there is something more to read.
     * @throws IOException if error occurred during reading the stream.
     */
    public boolean readAttempt() throws IOException {
        if (is.available() <= 0) {
            return false;
        }
        int i = is.read(buffer, 0, 1024);
        return acceptChunk(i);
    }

    /**
     * Reads some chunk of data from the {@link InputStream}.
     *
     * @return true if there is something more to read.
     * @throws InterruptedIOException if file is end.
     */
    public boolean fileReadAttempt() throws IOException {
        int i = is.read(buffer, 0, 1024);
        if (acceptChunk(i)) {
            return true;
        }
        throw new InterruptedIOException("EoF");
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    private boolean acceptChunk(int i) {
        if (i >= 0) {
            String chunk = new String(buffer, 0, i);
            if (collection == null) {
                collection = collector.supplier().get();
            }
            collector.accumulator().accept(collection, chunk);
            return true;
        }
        return false;
    }

    /**
     * Reads all the remaining data from the {@link InputStream}.
     * Closes stream.
     *
     * @return content what was read.
     * @throws IOException if error occurred during reading the stream.
     */
    public Optional<R> flush() throws IOException {
        boolean hasData;
        do {
            hasData = readAttempt();
        } while (hasData);
        closeQuietly(is);
        return Optional.ofNullable(collection).map(c -> collector.finisher().apply(c));
    }

    @Override
    public void close() {
        closeQuietly(is);
    }
}
