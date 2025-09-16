package mqtt2modbus.file;

import mqtt2modbus.modbus.IModbusHandler;
import java.io.IOException;


public interface IFileHandler {
    void readFile(String filename) throws IOException;
    void dataFiltering(int deviceType, String topic, int[] valuesSensorInfo, IModbusHandler modbusHandler);
    String[] getAllTopics();
}
