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

import java.util.concurrent.*;

import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModbusHandler implements IModbusHandler {

    private static final Logger logger = LogManager.getLogger(ModbusHandler.class);

    private ModbusTcpMaster master;
    private final String host;
    private final int port;
    private final int slaveId;
    private volatile boolean connected = false;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ModbusHandler(String host, int port, int slaveId) {
        this.host = host;
        this.port = port;
        this.slaveId = slaveId;

        connectWithRetry();
        scheduler.scheduleAtFixedRate(this::connectWithRetry, 1, 20, TimeUnit.SECONDS);
    }


    private void connectWithRetry() {
        if (connected) return;

        try {
            ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(host)
                    .setPort(port)
                    .build();

            master = new ModbusTcpMaster(config);
            master.connect().get(5, TimeUnit.SECONDS);

            connected = true;
            logger.info("Uspešno povezan na Modbus {}:{} (slaveId={})", host, port, slaveId);

        } catch (Exception e) {
            connected = false;
            logger.warn("Neuspešno povezivanje na Modbus {}:{} - {}", host, port, e.getMessage());
            master = null;
        }
    }

    public void write(int startAdress, int[] values) {
        if (!connected || master == null) {
            logger.error("Modbus nije povezan.");
            return;
        }
        if (values.length == 1)
            writesingle(startAdress, values[0]);
        else
            writeMulti(startAdress, values);
    }

    private void writeMulti(int address, int[] values) {
        if (!connected || master == null) {
            logger.error("Modbus nije povezan.");
            return;
        }

        try {
            ByteBuf buf = Unpooled.buffer(values.length * 2);
            for (int val : values) {
                buf.writeShort(val);
            }

            WriteMultipleRegistersRequest req =
                    new WriteMultipleRegistersRequest(address, values.length, buf);

            CompletableFuture<WriteMultipleRegistersResponse> future = master.sendRequest(req, slaveId);

            future.whenComplete((resp, ex) -> {
                if (ex != null) {
                    connected = false;
                    logger.error("Greška pri upisu više vrednosti {}", ex.getMessage());
                } else {
                    logger.info("Uspešno upisane vrednosti");
                }
            });

        } catch (Exception e) {
            connected = false;
            logger.error("Greška pri upisu više vrednosti");
        }
    }

    private void writesingle(int adress, int value) {
        try {
            WriteSingleRegisterRequest req = new WriteSingleRegisterRequest(adress, value);
            CompletableFuture<WriteSingleRegisterResponse> future = master.sendRequest(req, slaveId);

            future.whenComplete((resp, ex) -> {
                if (ex != null) {
                    connected = false;
                    logger.error("Greška pri upisu više vrednosti {}", ex.getMessage());
                } else {
                    logger.info("Uspešno upisana vrednost");
                }
            });

            /*future.thenAccept(response -> logger.info("Uspešno upisana vrednost"));
        }catch (Exception e) {
            logger.error(e.getMessage(), "Greška pri upisu vrednosti", e);*/

        } catch (Exception e) {
            connected = false;
            logger.error("Greška pri upisu više vrednosti");
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

                    logger.info("Pročitani registri: {}", values);
                } else
                    logger.warn("Nema odgovora pri čitanju registara od {}, {} broj registara", adress, quantity);
            });
        } catch (Exception e) {
            logger.error("Greška pri čitanju registara od {}, {} broj registara: {}", adress, quantity, e.getMessage(), e);
        }
    }
}



