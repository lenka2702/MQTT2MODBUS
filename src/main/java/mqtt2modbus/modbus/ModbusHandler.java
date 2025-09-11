package mqtt2modbus.modbus;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ModbusHandler {
    private final ModbusTcpMaster master;
    private final int slaveId;

    public ModbusHandler(String host, int port, int slaveId) throws ExecutionException, InterruptedException {
        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(host)
                .setPort(port)
                .build();
        this.master = new ModbusTcpMaster(config);
        this.master.connect().get();
        this.slaveId = slaveId;
    }

    public void writeValues(int startAddress, ByteBuf buf) {
        try {
            WriteMultipleRegistersRequest req = new WriteMultipleRegistersRequest(
                    startAddress, buf.readableBytes() / 2, buf);
            CompletableFuture<WriteMultipleRegistersResponse> future = master.sendRequest(req, slaveId);
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
