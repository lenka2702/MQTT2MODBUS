package mqtt2modbus;

import mqtt2modbus.file.IFileHandler;
import mqtt2modbus.modbus.IModbusHandler;
import mqtt2modbus.mqtt.IMqttHandler;
import mqtt2modbus.mqtt.MqttHandler;
import mqtt2modbus.modbus.ModbusHandler;
import mqtt2modbus.file.FileHandler;

public class main {
    public static void main(String[] args) {
        try {

            String broker = "tcp://localhost:1883";
            String fileName = "DataForDevices.csv";

            IFileHandler fileHandler = new FileHandler();
            fileHandler.readFile(fileName);
            String [] topics = fileHandler.getAllTopics();

            IModbusHandler modbusHandler = new ModbusHandler("127.0.0.1", 502, 1);

            IMqttHandler mqttHandler = new MqttHandler(broker, topics, modbusHandler, fileHandler);
            mqttHandler.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

