package com.mkkl.hantekapi.capture;

import com.mkkl.hantekapi.InitializeScopeExtension;
import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.OscilloscopeHandle;
import com.mkkl.hantekapi.communication.readers.BufferedCallback;
import com.mkkl.hantekapi.communication.readers.ByteArrayCallback;
import com.mkkl.hantekapi.communication.readers.UsbDataListener;
import com.mkkl.hantekapi.communication.readers.async.AsyncScopeDataReader;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

@ExtendWith(InitializeScopeExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AsyncCaptureTest {
    static AsyncScopeDataReader asyncScopeDataReader;
    static OscilloscopeHandle oscilloscopeHandle;

    @BeforeAll
    static void setUp() {
        System.out.println("before all async");
        oscilloscopeHandle = InitializeScopeExtension.getOscilloscopeHandle();
        asyncScopeDataReader = new AsyncScopeDataReader(oscilloscopeHandle);
    }

    @AfterAll
    static void close() throws InterruptedException, IOException {
        asyncScopeDataReader.waitToFinish();
        asyncScopeDataReader.close();
    }

    @ParameterizedTest
    @Order(1)
    @ValueSource(shorts = {512, 1024, 2048})
    public void readingBufferTest(short size) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        UsbDataListener listener = new BufferedCallback() {
            @Override
            public void onDataReceived(ByteBuffer byteBuffer) {
                Assertions.assertEquals(byteBuffer.remaining(), size);
                int countOfZeros = 0;
                while(byteBuffer.hasRemaining()) {
                    if(byteBuffer.get() == 0)
                        countOfZeros++;
                    else countOfZeros = 0;

                    if(countOfZeros > 128) Assertions.fail("Buffer had too many 0 in row");
                }
                latch.countDown();

            }
        };
        asyncScopeDataReader.registerListener(listener);
        asyncScopeDataReader.read(size);
        latch.await();
        asyncScopeDataReader.unregisterListener(listener);
    }

    @ParameterizedTest
    @Order(2)
    @ValueSource(shorts = {512, 1024, 2048})
    public void readingArrayTest(short size) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        UsbDataListener listener = new ByteArrayCallback() {
            @Override
            public void onDataReceived(byte[] data) {
                Assertions.assertEquals(data.length, size);
                int countOfZeros = 0;
                for(byte b : data) {
                    if(b == 0)
                        countOfZeros++;
                    else countOfZeros = 0;

                    if(countOfZeros > 128) Assertions.fail("Byte array had too many 0 in row");
                }
                latch.countDown();
            }
        };
        asyncScopeDataReader.registerListener(listener);
        asyncScopeDataReader.read(size);
        latch.await();
        asyncScopeDataReader.unregisterListener(listener);
    }
}
