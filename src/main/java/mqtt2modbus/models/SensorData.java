package mqtt2modbus.models;

public class SensorData {
    private String sensorId;
    private int deviceType;
    private int batteryLevel;
    private EnvironmentalData EnvironmentalData;

    public SensorData(String  sensorId, int deviceType, int batteryLevel, EnvironmentalData EnvironmentalData) {
        this.sensorId = sensorId;
        this.deviceType = deviceType;
        this.batteryLevel = batteryLevel;
        this.EnvironmentalData = EnvironmentalData;
    }

    public String getSensorId() { return sensorId; }
    public int getDeviceType() { return deviceType; }
    public int getBatteryLevel() { return batteryLevel; }
    public EnvironmentalData getEnvironmentalData() { return EnvironmentalData; }

    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public void setDeviceType(int deviceType) { this.deviceType = deviceType; }
    public void setBatteryLevel(int batteryLevel) { this.batteryLevel = batteryLevel; }
    public void setEnvironmentalData(EnvironmentalData environmentalData) { this.EnvironmentalData = environmentalData; }

}
