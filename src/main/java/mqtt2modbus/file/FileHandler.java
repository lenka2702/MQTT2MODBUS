package mqtt2modbus.file;
import mqtt2modbus.modbus.IModbusHandler;
import mqtt2modbus.modbus.ModbusHandler;
import mqtt2modbus.models.FileData;
import mqtt2modbus.models.SensorData;
import mqtt2modbus.models.SensorInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FileHandler implements IFileHandler {

    private static final Logger logger = LogManager.getLogger(FileHandler.class);
    private final Map<String, FileData> dataMap = new HashMap<>();

    @Override
    public void readFile(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            String[] data = line.split("\\s*,\\s*");

            try {
                FileData filedata = new FileData();

                filedata.setDeviceType(Integer.parseInt(data[0].trim()));
                filedata.setTopic(data[1].trim());
                filedata.setStartAdr(Integer.parseInt(data[2].trim()));
                filedata.setNumReg(Integer.parseInt(data[3].trim()));
                filedata.setSlaveID(Integer.parseInt(data[4].trim()));

                String key = filedata.getDeviceType() + "|" + filedata.getTopic();
                dataMap.put(key, filedata);

                logger.debug("Učitana konfiguracija: {}", key);

            }catch (NumberFormatException e){
                logger.warn("Nevalidan red u fajlu '{}': {}", filename, line, e);
            }
        }

    }


    @Override
    public void dataFiltering(int deviceType, String topic, int[] valuesSensorInfo, IModbusHandler modbusHandler) {

        String key = deviceType + "|" + topic;
        FileData filedata = dataMap.get(key);

        if (filedata != null) {
            int startAdr = filedata.getStartAdr();
            int allowedRegs = filedata.getNumReg();

            if(valuesSensorInfo.length <= allowedRegs)
                modbusHandler.write(startAdr, valuesSensorInfo);
            else
                logger.warn("Poslato vise vrednosti nego sto konfiguracija podrzava za topic={} (dozvoljeno={}, dobijeno={})", topic, allowedRegs, valuesSensorInfo.length);
        }else
            logger.error("Nema konfiguracije za primljenu poruku: deviceType={}, topic={}", deviceType, topic);

    }

    @Override
    public String[] getAllTopics() {
        Set<String> topics = new HashSet<>();
        for (FileData fileData : dataMap.values()) {
            topics.add(fileData.getTopic().trim());
        }
        logger.info("Teme na koje postoji subscribe: {}", topics);
        return topics.toArray(new String[0]);

    }


}
