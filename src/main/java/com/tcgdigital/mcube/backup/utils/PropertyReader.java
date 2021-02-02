package com.tcgdigital.mcube.backup.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PropertyReader {

    private static Properties prop = new Properties();
    private static final Logger log = LoggerFactory.getLogger(PropertyReader.class);

    static {

        String userHome = System.getProperty("user.dir");
        log.info("user dir: " + userHome);
        try {
            String fileLocation = userHome + File.separator + "backup.properties";
            log.info(fileLocation);
            prop.load(new FileInputStream(fileLocation));
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    public String getProperty(String key) {

        if (prop.getProperty("detailed.log.enabled").equalsIgnoreCase("Y")) {
            log.info(key + " : " + prop.getProperty(key));
        }
        return prop.getProperty(key);
    }

    public void setProperty(String key, String value) {
        prop.setProperty(key, value);
    }

}
