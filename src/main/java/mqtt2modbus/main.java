package mqtt2modbus;

import mqtt2modbus.config.Config;
import mqtt2modbus.config.ConfigLoader;
import mqtt2modbus.file.IFileHandler;
import mqtt2modbus.modbus.IModbusHandler;
import mqtt2modbus.mqtt.IMqttHandler;
import mqtt2modbus.mqtt.MqttHandler;
import mqtt2modbus.modbus.ModbusHandler;
import mqtt2modbus.file.FileHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class main {

    private static final Logger logger = LogManager.getLogger(main.class);

    public static void main(String[] args) {
        try {

            Config config = ConfigLoader.loadConfig();
            logger.debug("Konfiguracija učitana: {}", config);

            IFileHandler fileHandler = new FileHandler();
            fileHandler.readFile(config.getFileName());
            String [] topics = fileHandler.getAllTopics();

            IModbusHandler modbusHandler = new ModbusHandler(config.getHost(), config.getPort(), config.getSlaveID());
            logger.info("ModbusHandler uspešno inicijalizovan.");

            IMqttHandler mqttHandler = new MqttHandler(config.getBroker(), topics, modbusHandler, fileHandler);
            mqttHandler.start();

        } catch (Exception e) {
            logger.fatal("Greška pri pokretanju aplikacije: {}", e.getMessage(), e);
        }
    }
}

