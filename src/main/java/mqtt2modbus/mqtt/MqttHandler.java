package mqtt2modbus.mqtt;

import com.google.gson.Gson;
import mqtt2modbus.modbus.ModbusHandler;
import mqtt2modbus.models.SensorData;
import mqtt2modbus.models.SensorInfo;
import org.eclipse.paho.client.mqttv3.*;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;

public class MqttHandler implements IMqttHandler {

    private final String broker;
    private final String[] topics;
    private final Gson gson = new Gson();
    private final Map<String, SensorInfo> sensorMap = new HashMap<>();
    private int brojac = 0;
    private final ModbusHandler modbusHandler;

    public MqttHandler(String broker, String[] topic, ModbusHandler modbusHandler) {
        this.broker = broker;
        this.topics = topic;
        this.modbusHandler = modbusHandler;
    }


    public void start() {
        try {
            String clientId = MqttClient.generateClientId();
            MqttClient client = new MqttClient(broker, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    System.out.println("Subscriber povezan na: " + serverURI + (reconnect ? " (reconnect)" : ""));
                    try {

                        for (String topic : topics) {
                            client.subscribe(topic, (t,mqttMessage) -> handleMessage(t, mqttMessage));
                            System.out.println("Resub na temu: " + topic);
                        }

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Veza izgubljena: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {}

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            client.connect(options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String topic, MqttMessage mqttMessage) {
        String message = new String(mqttMessage.getPayload());

        try {
            SensorData data = gson.fromJson(message, SensorData.class);
            if (data != null) {
                System.out.println("[" + topic + "] Primljeno: " + message);

                if (data.getSensorId() != null && data.getEnvironmentalData() != null && data.getEnvironmentalData().getValues() != null) {

                    SensorInfo info = new SensorInfo(data.getDeviceType(), data.getEnvironmentalData().getValues());
                    sensorMap.put(data.getSensorId(), info);

                    int[] valuesSensorInfo = info.getValues();
                    modbusHandler.write(brojac * valuesSensorInfo.length , valuesSensorInfo);
                    brojac++;

                }
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Nije validan JSON: " + message);
        }
    }

}
