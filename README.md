# Hantek Java Api
Port of [Hantek6022API](https://github.com/Ho-Ro/Hantek6022API) python api for java.
## Examples
Find device with
```java
Oscilloscope oscilloscope = OscilloscopeManager.findAndGetFirst(OscilloscopeDevices.DSO6022BE);
```
Then flash firmware
```java
if (!oscilloscope.isFirmwarePresent()) {
    oscilloscope.flash_firmware();
    while(oscilloscope == null || !oscilloscope.isFirmwarePresent()) {
        Thread.sleep(100);
        oscilloscope = OscilloscopeManager.findAndGetFirst(OscilloscopeDevices.DSO6022BE);
    }
}
```
Set your parameters
```java
oscilloscope.setActiveChannels(ActiveChannels.CH1CH2);
oscilloscope.setSampleRate(SampleRates.SAMPLES_100kS_s);
oscilloscope.getChannel(Channels.CH1).setVoltageRange(VoltageRange.RANGE5000mV);
oscilloscope.getChannel(Channels.CH2).setVoltageRange(VoltageRange.RANGE5000mV);
oscilloscope.getChannel(Channels.CH1).setProbeMultiplier(10);
oscilloscope.getChannel(Channels.CH2).setProbeMultiplier(10);
```
Get calibration data from oscilloscope's eeprom
```java
oscilloscope.setCalibration(oscilloscope.readCalibrationValues());
```
Read data from device
```java
ScopeDataReader scopeDataReader = oscilloscope.createDataReader();
byte[] bytes = scopeDataReader.syncRead((short) 1024);
int readBytes = 0;
while (readBytes < bytes.length) {
    float[] channelData = input.readFormattedVoltages();
    System.out.printf("CH1=%.2fV CH2=%.2fV\n", channelData[0], channelData[1]);
    readBytes+=2;
}
```
## Todo
* Support for AC modification
* Isochronous transfer mode
* Firmware reading from eeprom
* Get library to work on android
* Getting started tab