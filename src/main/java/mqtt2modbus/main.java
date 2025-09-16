package mqtt2modbus;

import mqtt2modbus.config.Config;
import mqtt2modbus.config.ConfigLoader;
import mqtt2modbus.file.IFileHandler;
import mqtt2modbus.modbus.IModbusHandler;
import mqtt2modbus.mqtt.IMqttHandler;
import mqtt2modbus.mqtt.MqttHandler;
import mqtt2modbus.modbus.ModbusHandler;
import mqtt2modbus.file.FileHandler;

import java.util.Arrays;

public class main {
    public static void main(String[] args) {
        try {

            Config config = ConfigLoader.loadConfig();

            IFileHandler fileHandler = new FileHandler();
            fileHandler.readFile(config.getFileName());
            String [] topics = fileHandler.getAllTopics();

            System.out.println(config.getSlaveID());

            IModbusHandler modbusHandler = new ModbusHandler(config.getHost(), config.getPort(), config.getSlaveID());

            IMqttHandler mqttHandler = new MqttHandler(config.getBroker(), topics, modbusHandler, fileHandler);
            mqttHandler.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

