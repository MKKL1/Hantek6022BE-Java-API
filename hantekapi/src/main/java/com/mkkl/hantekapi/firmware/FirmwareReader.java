package com.mkkl.hantekapi.firmware;

import java.io.*;
import java.util.ArrayList;
import java.util.HexFormat;

//TODO read any firmware, either from file or byte array

public class FirmwareReader extends Reader {
    private final BufferedReader bufferedReader;

    public FirmwareReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    /**
     * Reads and parses line of hex file to {@link FirmwareControlPacket}
     * @return Returns FirmwareControlPacket on success or null on end of line
     */
    public FirmwareControlPacket readRecordLine() throws IOException {
        String line = bufferedReader.readLine().strip();
        if (!line.startsWith(":")) throw new IOException("Line doesn't start with ':'");
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(HexFormat.of().parseHex(line.substring(1))));

        byte size = dataInputStream.readByte();
        short address = dataInputStream.readShort();
        byte type = dataInputStream.readByte();
        byte[] data = dataInputStream.readNBytes(size);
        byte file_checksum = dataInputStream.readByte();
        if (type == 0x00) {
            byte checksum = (byte) ((sum(data) + size + (address%256) +  (address >> 8)) % 256);
            if ((((checksum + file_checksum) % 256 ) & 0xFF) != 0) throw new IOException("Invalid checksum");
            return new FirmwareControlPacket(size, address, data);
        } else if (type == 0x01) {
            if (file_checksum != (byte)0xFF) throw new IOException("Invalid checksum for record of type 0x01");
            return null;
        } else throw new IOException("Unknown record type of " + type);
    }

    /**
     * Reads entire file
     * @return array of all packets read from file
     * @throws IOException
     */
    public FirmwareControlPacket[] readAll() throws IOException {
        ArrayList<FirmwareControlPacket> recordLines = new ArrayList<FirmwareControlPacket>();
        FirmwareControlPacket recordLine;
        while((recordLine = readRecordLine()) != null) {
            recordLines.add(recordLine);
        }
        return recordLines.toArray(new FirmwareControlPacket[0]);
    }

    private static int sum(byte[] array) {
        int result = 0;
        for (final byte v : array) {
            result += v;
        }
        return result;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return bufferedReader.read();
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
