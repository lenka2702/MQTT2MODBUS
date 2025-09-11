package mqtt2modbus;

import mqtt2modbus.mqtt.MqttHandler;
import mqtt2modbus.modbus.ModbusHandler;

public class main {
    public static void main(String[] args) {
        try {

            String broker = "tcp://localhost:1883";
            String[] topics = {"publisher1/data", "senzori/temperatura"};

            // init modbus
            ModbusHandler modbusHandler = new ModbusHandler("127.0.0.1", 502, 1);

            // init MQTT
            MqttHandler mqttHandler = new MqttHandler(broker, topics, modbusHandler);
            mqttHandler.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

