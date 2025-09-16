package mqtt2modbus.config;

public class Config {
    private String broker;
    private String fileName;
    private String host;
    private int port;
    private int slaveID;

    public String getBroker() { return broker; }
    public void setBroker(String broker) { this.broker = broker; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public int getSlaveID() { return slaveID; }
    public void setSlaveID(int slaveID) { this.slaveID = slaveID; }
}

