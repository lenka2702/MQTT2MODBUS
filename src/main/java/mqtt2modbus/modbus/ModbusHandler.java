package mqtt2modbus.modbus;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.requests.WriteSingleRegisterRequest;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse;
import com.digitalpetri.modbus.responses.WriteSingleRegisterResponse;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import io.netty.buffer.Unpooled;

public class ModbusHandler implements IModbusHandler {

    private final ModbusTcpMaster master;
    private final int slaveId;

    public ModbusHandler(String host, int port, int slaveId) throws ExecutionException, InterruptedException {
        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(host)
                .setPort(port)
                .build();
        this.master = new ModbusTcpMaster(config);
        this.master.connect().get();
        this.slaveId = slaveId;
    }//ovde ce trebati neki exepcion jer baca gresku jer nije pokrenuta simulacija

    public void write(int startAdress, int[] values) {
        if(values.length == 1)
            writesingle(startAdress, values[0]);
        else
            writeMulti(startAdress, values);
    }

    private void writeMulti(int adress, int [] values) {
        try {
            ByteBuf buf = Unpooled.buffer(values.length * 2);
            for (int val : values)
                buf.writeShort(val);
            WriteMultipleRegistersRequest req = new WriteMultipleRegistersRequest(
                    adress, values.length, buf);
            CompletableFuture<WriteMultipleRegistersResponse> future = master.sendRequest(req, slaveId);
            future.thenAccept(response -> {});


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writesingle(int adress, int value) {
        try {
            WriteSingleRegisterRequest req = new WriteSingleRegisterRequest(adress, value);
            CompletableFuture<WriteSingleRegisterResponse> future = master.sendRequest(req, slaveId);
            future.thenAccept(response -> {});
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void read(int adress, int quantity) {
        try {
            ReadHoldingRegistersRequest req = new ReadHoldingRegistersRequest(adress, quantity);
            CompletableFuture<ReadHoldingRegistersResponse> future = master.sendRequest(req, slaveId);

            future.thenAccept(resp -> {
                if (resp != null) {
                    ByteBuf buf = resp.getRegisters();
                    int[] values = new int[quantity];

                    for (int i = 0; i < quantity; i++)
                        values[i] = buf.readShort();

                    System.out.print("Pročitani registri: ");
                    for (int val : values)
                        System.out.print(val + " ");
                    System.out.println();

                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


}
