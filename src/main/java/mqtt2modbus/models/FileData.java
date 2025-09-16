package mqtt2modbus.models;

public class FileData {
    public int deviceType;
    public String topic;
    public int startAdr;
    public int numReg;
    public int slaveID;


    public int getDeviceType() {return deviceType;}
    public String getTopic() {return topic;}
    public int getStartAdr() {return startAdr;}
    public int getNumReg() {return numReg;}
    public int getSlaveID(){return slaveID;}

    public void setDeviceType(int deviceType) {this.deviceType = deviceType;}
    public void setTopic(String topic) {this.topic = topic;}
    public void setStartAdr(int startAdr) {this.startAdr = startAdr;}
    public void setNumReg(int numReg) {this.numReg = numReg;}
    public void setSlaveID(int slaveID) {this.slaveID = slaveID;}
}
