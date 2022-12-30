package com.mkkl.hantekapi.channel;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.constants.VoltageRange;

import javax.usb.UsbException;
import java.util.HashMap;


public class ScopeChannel {
    private static final VoltageRange[] voltageRanges = VoltageRange.values();
    public static final byte[] calibrationOffsets = {0, 6, 8, 14};
    public static final byte[] calibrationGainOff = {0, 4, 8, 14};

    private final int id;
    private final HashMap<VoltageRange, Float> offsets = new HashMap<>();
    private final HashMap<VoltageRange, Float> gains;
    private VoltageRangeChange voltageRangeChangeEvent = null;

    //Save data from hashmap to variable each time a voltage range is changed
    private float _offset;
    private float _gain;
    private float scale_factor = 1;

    private VoltageRange currentVoltageRange;
    private int probeMultiplier = 1;
    private float additionalOffset = 0;

    public float currentData;

    public ScopeChannel(int id, VoltageRangeChange voltageRangeChangeEvent) throws UsbException {
        this(id);
        this.voltageRangeChangeEvent = voltageRangeChangeEvent;
    }

    public ScopeChannel(int id) throws UsbException {
        this.id = id;
        for(VoltageRange range : voltageRanges) offsets.put(range, 0f);
        gains = new HashMap<>(){{
                put(VoltageRange.RANGE5000mV, 1.01f);
                put(VoltageRange.RANGE2500mV, 1.01f);
                put(VoltageRange.RANGE1000mV, 0.99f);
                put(VoltageRange.RANGE250mV, 1f);}};

        setVoltageRange(VoltageRange.RANGE5000mV);
    }

    public void setOffsets(float[] newoffsets) {
        for (int i = 0; i < newoffsets.length; i++) {
            offsets.put(voltageRanges[i], newoffsets[i]);
        }
        recalculate_scalefactor();
    }

    public void setGains(float[] newoffsets) {
        for (int i = 0; i < newoffsets.length; i++) {
            gains.put(voltageRanges[i], newoffsets[i]);
        }
        recalculate_scalefactor();
    }

    private void recalculate_scalefactor() {
        _offset = offsets.get(currentVoltageRange);
        _gain = gains.get(currentVoltageRange);
        scale_factor = ((5.12f * probeMultiplier * _gain) / (float)(currentVoltageRange.getGain() << 7));
    }

    public float formatData(byte rawdata) {
        return ((rawdata+128) - (_offset + additionalOffset)) * scale_factor;
    }

    public int getId() {
        return id;
    }

    public int getChannelNumber() {
        return id+1;
    }

    public HashMap<VoltageRange, Float> getOffsets() {
        return offsets;
    }

    public HashMap<VoltageRange, Float> getGains() {
        return gains;
    }

    public VoltageRange getVoltageRange() {
        return currentVoltageRange;
    }

    public void setVoltageRange(VoltageRange currentVoltageRange) throws UsbException {
        this.currentVoltageRange = currentVoltageRange;
        recalculate_scalefactor();
        if(voltageRangeChangeEvent != null) voltageRangeChangeEvent.onVoltageChange(currentVoltageRange, id);
    }

    public int getProbeMultiplier() {
        return probeMultiplier;
    }

    public void setProbeMultiplier(int probeMultiplier) {
        this.probeMultiplier = probeMultiplier;
        recalculate_scalefactor();
    }

    public float getAdditionalOffset() {
        return additionalOffset;
    }

    public void setAdditionalOffset(float additionalOffset) {
        this.additionalOffset = additionalOffset;
    }

    @Override
    public String toString() {
        System.out.println(offsets);
        System.out.println(gains);
        return "ScopeChannel{" +
                "id=" + id +
                ", _offset=" + _offset +
                ", _gain=" + _gain +
                ", scale_factor=" + scale_factor +
                ", currentVoltageRange=" + currentVoltageRange +
                ", probeMultiplier=" + probeMultiplier +
                ", additionalOffset=" + additionalOffset +
                '}';
    }
}
