package mqtt2modbus.models;

public class SensorInfo {
    private int deviceType;
    private int[] values;

     public SensorInfo(int deviceType, int[] values) {
        this.deviceType = deviceType;
        this.values = values;
    }

    public int getDeviceType() {return deviceType;}
    public int[] getValues() {return values;}

    public void setValues(int[] values) {this.values = values;}
    public void setDeviceType(int deviceType) {this.deviceType = deviceType;}


}
