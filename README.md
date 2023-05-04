# Hantek Java Api
Port of [Hantek6022API](https://github.com/Ho-Ro/Hantek6022API) python api for java.
## Examples
Find and flash device
```java
Oscilloscope oscilloscope = ScopeUtils.getAndFlashFirmware(HantekDeviceType.DSO6022BE);
```
Set your parameters
```java
OscilloscopeHandle oscilloscopeHandle = oscilloscope.setup();
oscilloscopeHandle.setActiveChannels(ActiveChannels.CH1CH2);
oscilloscopeHandle.setSampleRate(SampleRates.SAMPLES_100kS_s);
oscilloscopeHandle.getChannel(Channels.CH2).setVoltageRange(VoltageRange.RANGE5000mV);
oscilloscopeHandle.getChannel(Channels.CH2).setProbeAttenuation(10);
```
Get calibration data from oscilloscope's eeprom
```java
oscilloscopeHandle.setCalibration(oscilloscopeHandle.readCalibrationValues());
```
Read data from device
```java
SyncScopeDataReader syncScopeDataReader = oscilloscopeHandle.createSyncDataReader();
byte[] bytes = syncScopeDataReader.readToByteArray((short) 1024);
AdcInputStream input = AdcInputStream.create(new ByteArrayInputStream(bytes), oscilloscopeHandle);

for (int readBytes = 0; readBytes < bytes.length; readBytes += 2) {
    float[] f = input.readFormattedVoltages();
    System.out.printf("CH1=%.2fV CH2=%.2fV\n", f[0], f[1]);
}
```
## Todo
* Support for AC modification
* Isochronous transfer mode
* Firmware reading from eeprom
* Get library to work on android
* Getting started tab
