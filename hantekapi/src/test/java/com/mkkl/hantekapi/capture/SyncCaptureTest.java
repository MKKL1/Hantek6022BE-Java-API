package com.mkkl.hantekapi.capture;

import com.mkkl.hantekapi.InitializeScopeExtension;
import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.readers.sync.SyncScopeDataReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.ByteBuffer;

@ExtendWith(InitializeScopeExtension.class)
public class SyncCaptureTest {
    static SyncScopeDataReader syncScopeDataReader;
    static Oscilloscope oscilloscope;

    @BeforeAll
    static void setUp() {
        oscilloscope = InitializeScopeExtension.getOscilloscope();
        syncScopeDataReader = new SyncScopeDataReader(oscilloscope);
    }

    @AfterAll
    static void close() throws IOException {
        syncScopeDataReader.close();
    }

    @ParameterizedTest
    @ValueSource(shorts = {512, 1024, 2048})
    public void readingBufferTest(short size) {
        ByteBuffer byteBuffer = syncScopeDataReader.readToBuffer(size);
        Assertions.assertEquals(byteBuffer.remaining(), size);
        int countOfZeros = 0;
        while(byteBuffer.hasRemaining()) {
            if(byteBuffer.get() == 0)
                countOfZeros++;
            else countOfZeros = 0;

            if(countOfZeros > 128) Assertions.fail("Buffer had too many 0 in row");
        }
    }

    @ParameterizedTest
    @ValueSource(shorts = {512, 1024, 2048})
    public void readingByteArray(short size) {
        byte[] byteArray = syncScopeDataReader.readToByteArray(size);
        Assertions.assertEquals(byteArray.length, size);
        int countOfZeros = 0;
        for(byte b : byteArray) {
            if(b == 0)
                countOfZeros++;
            else countOfZeros = 0;

            if(countOfZeros > 128) Assertions.fail("Byte array had too many 0 in row");
        }
    }
}
