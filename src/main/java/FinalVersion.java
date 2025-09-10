import org.eclipse.paho.client.mqttv3.*;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class SensorDataNew {
    String sensorId;
    int deviceType;
    int batteryLevel;
    EnvironmentalData EnvironmentalData;
}

class EnvironmentalData{
    int fireRiskLevel;
    int[] values = new int[5];
}

class SensorInfo{
    int deviceType;
    int[] values;

    public SensorInfo(int deviceType, int[] values){
        this.deviceType = deviceType;
        this.values = values;
    }
}

public class FinalVersion {

    private static Map<String, SensorInfo> sensorMap = new HashMap<>();
    public static int brojac;

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String[] topic =  {"publisher1/data", "senzori/temperatura"};
        int ID = 1;
        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder("127.0.0.1")
                .setPort(502)
                .build();

        ModbusTcpMaster master = new ModbusTcpMaster(config);
        master.connect().get();

        Gson gson = new Gson();

        brojac = 0;

        try {
            MqttClient client = new MqttClient(broker, clientId);
            client.connect();

            //System.out.println("Sub na temu: " + topic);
            for(int i = 0; i < topic.length; i++) { //ovo mi se ne svidja kako sam napisala

                System.out.println("Sub na temu: " + topic[i]);

                int prom = i;
                client.subscribe(topic[i], new IMqttMessageListener() {
                    @Override
                    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                        String message = new String(mqttMessage.getPayload());
                        System.out.println(topic[prom] + "Primljeno" + message);

                        SensorDataNew data = gson.fromJson(message, SensorDataNew.class);

                        if(data.sensorId != null && data.EnvironmentalData != null) {
                            SensorInfo info = new SensorInfo(data.deviceType, data.EnvironmentalData.values);

                            sensorMap.put(data.sensorId, info);

                            ByteBuf buf = Unpooled.buffer(2*5);
                            for(int i : info.values)
                                buf.writeShort(i);

                            WriteMultipleRegistersRequest multiReq = new WriteMultipleRegistersRequest(brojac*5,buf.readableBytes()/2,buf);
                            CompletableFuture<WriteMultipleRegistersResponse> multiFuture = master.sendRequest(multiReq, ID);
                            brojac = brojac +1;

                        }

                    }
                });
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
