package mqtt2modbus.mqtt;

import com.google.gson.Gson;
import mqtt2modbus.models.SensorData;
import mqtt2modbus.models.SensorInfo;
import mqtt2modbus.modbus.ModbusHandler;
import org.eclipse.paho.client.mqttv3.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

public class MqttHandler {

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


    public void start(){

        try {
            String clientId = MqttClient.generateClientId();
            MqttClient client = new MqttClient(broker, clientId);
            client.connect();


            for (int i = 0; i < topics.length; i++) {
                int topicIndex = i;
                System.out.println("Sub na temu: " + topics[i]);

                client.subscribe(topics[i], new IMqttMessageListener() {
                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) {
                        String message = new String(mqttMessage.getPayload());
                        System.out.println(topics[topicIndex] + " Primljeno: " + message);

                        SensorData data = gson.fromJson(message, SensorData.class);

                        if (data.sensorId != null && data.EnvironmentalData != null) {
                            SensorInfo info = new SensorInfo(data.deviceType, data.EnvironmentalData.values);
                            sensorMap.put(data.sensorId, info);

                            ByteBuf buf = Unpooled.buffer(2 * info.values.length);
                            for (int val : info.values) {
                                buf.writeShort(val);
                            }

                            modbusHandler.writeValues(brojac * 5, buf);
                            brojac++;
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
