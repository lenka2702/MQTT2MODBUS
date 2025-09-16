package mqtt2modbus.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;

public class ConfigLoader {

    public static Config loadConfig() {
        Yaml yaml = new Yaml();
        InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("config.yaml");
        if (inputStream == null)
            throw new RuntimeException("Nije pronađen config.yaml");

        return yaml.loadAs(inputStream, Config.class);
    }
}

