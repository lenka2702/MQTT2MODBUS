package mqtt2modbus.models;

public class EnvironmentalData {
    private int fireRiskLevel;
    private int[] values = new int[5];

    public EnvironmentalData(int fireRiskLevel, int[] values) {
        this.fireRiskLevel = fireRiskLevel;
        this.values = values;
    }

    public int getFireRiskLevel() {return fireRiskLevel;}
    public int[] getValues() {return values;}

    public void setFireRiskLevel(int value) {this.fireRiskLevel = value;}
    public void setValues(int[] value) {this.values = value;}
}
