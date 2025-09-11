package mqtt2modbus.models;

public class SensorInfo {
    public int deviceType;
    public int[] values;

    public SensorInfo(int deviceType, int[] values) {
        this.deviceType = deviceType;
        this.values = values;
    }
}
