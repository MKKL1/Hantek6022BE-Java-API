package com.mkkl.hantekapi;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.stream.Stream;

public class FirmwareReader extends BufferedReader {
    public FirmwareReader(Reader in) {
        super(in);
    }

    /**
     *
     * @return Returns FirmwareControlPacket on success or null on end of line
     * @throws IOException
     */
    public FirmwareControlPacket readRecordLine() throws IOException {
        String line = readLine().strip();
        if (!line.startsWith(":")) throw new IllegalArgumentException("Line doesn't start with ':'");
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(HexFormat.of().parseHex(line.substring(1))));

        byte size = dataInputStream.readByte();
        short address = dataInputStream.readShort();
        byte type = dataInputStream.readByte();
        byte[] data = dataInputStream.readNBytes(size);
        byte file_checksum = dataInputStream.readByte();
        if (type == 0x00) {
            byte checksum = (byte) ((sum(data) + size + (address%256) +  (address >> 8)) % 256);
            if ((((checksum + file_checksum) % 256 ) & 0xFF) != 0) throw new IllegalArgumentException("Invalid checksum");
            return new FirmwareControlPacket(size, address, data);
        } else if (type == 0x01) {
            if (file_checksum != (byte)0xFF) throw new IllegalArgumentException("Invalid checksum for record of type 0x01");
            return null;
        } else throw new IllegalArgumentException("Unknown record type of " + type);
    }

    public Firmware readFirmware() throws IOException {
        ArrayList<FirmwareControlPacket> recordLines = new ArrayList<FirmwareControlPacket>();

        recordLines.add(new FirmwareControlPacket((byte) 1, (short)0xe600, new byte[]{0x01}));
        FirmwareControlPacket recordLine;
        while((recordLine = readRecordLine()) != null) {
            recordLines.add(recordLine);
        }

        recordLines.add(new FirmwareControlPacket((byte) 1, (short)0xe600, new byte[]{0x00}));

        return new Firmware(recordLines);
    }

    private static int sum(byte[] array) {
        int result = 0;
        for (final byte v : array) {
            result += v;
        }
        return result;
    }
}
