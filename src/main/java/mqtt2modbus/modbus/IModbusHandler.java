package mqtt2modbus.modbus;

public interface IModbusHandler {
    void write(int startAdress, int[] values);
}
