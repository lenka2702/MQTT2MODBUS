package mqtt2modbus.mqtt;

import com.google.gson.Gson;
import mqtt2modbus.file.IFileHandler;
import mqtt2modbus.modbus.IModbusHandler;
import mqtt2modbus.models.SensorData;
import mqtt2modbus.models.SensorInfo;
import org.eclipse.paho.client.mqttv3.*;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MqttHandler implements IMqttHandler {

    private static final Logger logger = LogManager.getLogger(MqttHandler.class);

    private final String broker;
    private final String[] topics;
    private final Gson gson = new Gson();
    private final Map<String, SensorInfo> sensorMap = new HashMap<>();
    private final IModbusHandler modbusHandler;
    private final IFileHandler fileHandler;


    public MqttHandler(String broker, String[] topic, IModbusHandler modbusHandler, IFileHandler fileHandler) throws MqttException {
        if (broker == null || topic == null || modbusHandler == null || fileHandler == null) {
            logger.fatal("Bar jedan od parametara za kontruktor MqttHandelr je null");
            throw new IllegalArgumentException("Parametri konstruktora ne smeju biti null.");
        }
        this.broker = broker;
        this.topics = topic;
        this.modbusHandler = modbusHandler;
        this.fileHandler = fileHandler;
        logger.debug("MqttHandler uspešno inicijalizovan za broker: {}", broker);
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
                    logger.info("Povezan na: {} {}", serverURI, (reconnect ? "(reconnect)" : ""));
                    try {
                        for (String topic : topics) {
                            if (topic != null && !topic.isEmpty()){
                                client.subscribe(topic, (t, mqttMessage) -> handleMessage(t, mqttMessage));
                                logger.debug("Subscribovan na temu: {}", topic);
                            }else
                                logger.warn("Pokušaj subscribe na prazan topic");
                        }

                    } catch (MqttException e) {
                        logger.error("Greška prilikom subscribe-a na teme", e);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    logger.error("MQTT konekcija izgubljena: {}", cause.getMessage(), cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    logger.trace("Poruka stigla na topic {}: {}", topic, new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    logger.debug("MQTT isporuka završena za token: {}", token);
                }
            });

            client.connect(options);
            logger.info("MQTT klijent uspešno konektovan.");

        } catch (Exception e) {
            logger.fatal("Greška prilikom konektovanja na MQTT broker", e);
        }
    }

    private void handleMessage(String topic, MqttMessage mqttMessage) {
        if (mqttMessage == null || mqttMessage.getPayload() == null) {
            logger.warn("Primljena prazna MQTT poruka na topicu: {}", topic);
            return;
        }
        String message = new String(mqttMessage.getPayload());

        try {
            SensorData data = gson.fromJson(message, SensorData.class);
            if (data != null) {
                logger.info("[{}] Primljena validna poruka: {}", topic, message);

                if (data.getSensorId() != null && data.getEnvironmentalData() != null && data.getEnvironmentalData().getValues() != null) {

                    SensorInfo info = new SensorInfo(data.getDeviceType(), data.getEnvironmentalData().getValues());
                    sensorMap.put(data.getSensorId(), info);
                    int[] valuesSensorInfo = info.getValues();

                    try {
                        fileHandler.dataFiltering(data.getDeviceType(), topic, valuesSensorInfo, modbusHandler);
                    }catch (Exception e) {
                        logger.error("Greška u fileHandler.dataFiltering za: {}", data.getSensorId(), e);
                    }
                }else {
                    logger.warn("Primljeni podaci nisu kompletni: {}", message);
                }
            }else {
                logger.warn("Primljena poruka nije mogla biti mapirana u SensorData: {}", message);
            }
        } catch (JsonSyntaxException e) {
            logger.error("Nije validan JSON: {}", message, e);
        }catch (Exception e) {
            logger.error("Neočekivana greška prilikom obrade poruke: {}", message, e);
        }
    }

}
