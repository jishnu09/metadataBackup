package com.tcgdigital.mcube.backup.services;

import com.smattme.MysqlExportService;
import com.tcgdigital.mcube.backup.utils.PropertyReader;
import com.tcgdigital.mcube.backup.utils.enc.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;

public class DatabaseBackupTaker {
    private final Logger log = LoggerFactory.getLogger(DatabaseBackupTaker.class);

    private final PropertyReader props=new PropertyReader();

    public void takeBackup() {
        try {
            log.info("Database Backup start..........");
            Date startTime= Calendar.getInstance().getTime();
            List<String> databaseList=Arrays.asList(props.getProperty("backup.database.urls").split(","));
            databaseList.forEach(databaseUrl->{
                String database="";
                try {
                    Date startTime1= Calendar.getInstance().getTime();
                    Properties properties = new Properties();

                    properties.setProperty(MysqlExportService.JDBC_CONNECTION_STRING,
                            databaseUrl);
                    properties.setProperty(MysqlExportService.JDBC_DRIVER_NAME,
                            props.getProperty("backup.database.driver"));
                    String userName = props.getProperty("backup.database.username");
                    String passWord = props.getProperty("backup.database.password");

//                    properties.setProperty(MysqlExportService.DB_USERNAME, CryptoUtil.decrypt(userName,"1whathave3idone2"));
//                    properties.setProperty(MysqlExportService.DB_PASSWORD, CryptoUtil.decrypt(passWord,"1whathave3idone2"));

                    properties.setProperty(MysqlExportService.DB_USERNAME, userName);
                    properties.setProperty(MysqlExportService.DB_PASSWORD, passWord);

                    properties.setProperty(MysqlExportService.PRESERVE_GENERATED_ZIP, "true");


                    if (databaseUrl.contains("?")) {
                        database = databaseUrl.substring(databaseUrl.lastIndexOf("/") + 1, databaseUrl.indexOf("?"));
                    } else {
                        database = databaseUrl.substring(databaseUrl.lastIndexOf("/") + 1);
                    }
                    log.info("Taking backup of database: "+database);
                    String databaseBackupPath=props.getProperty("backup.database.path")+File.separator+database;
                    properties.setProperty(MysqlExportService.TEMP_DIR,
                            new File(databaseBackupPath).getPath());

                    File file=new File(databaseBackupPath);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    MysqlExportService mysqlExportService = new MysqlExportService(properties);
                    mysqlExportService.export();
                    reconcileFiles(databaseBackupPath);
                    Date endTime= Calendar.getInstance().getTime();
                    showDuration(startTime1,endTime,database+" Database backup");
                } catch (Exception e) {
                    log.error("",e);
                }
                log.info("[END]Taking backup of database: "+database);
            });


//            if (checkSmtp()) {
//                properties.setProperty(MysqlExportService.EMAIL_HOST, applicationConfig.getMcubeConfig().getString("smtp.host"));
//                properties.setProperty(MysqlExportService.EMAIL_PORT, "25");
//                properties.setProperty(MysqlExportService.EMAIL_USERNAME, "mailtrap-username");
//                properties.setProperty(MysqlExportService.EMAIL_PASSWORD, "mailtrap-password");
//                properties.setProperty(MysqlExportService.EMAIL_FROM, "test@smattme.com");
//                properties.setProperty(MysqlExportService.EMAIL_TO, "backup@smattme.com");
//            }


            Date endTime= Calendar.getInstance().getTime();
            showDuration(startTime,endTime," Full Database backup");
            log.info("..........Database Backup end");
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void reconcileFiles(String databaseBackupPath){
        try{
            File dir = new File(databaseBackupPath);
            File[] files = dir.listFiles();
            int fileCount= Integer.parseInt(props.getProperty("backup.database.file.count"));
            if (files != null) {
                log.info("Descending order.");
                Arrays.sort(files, LASTMODIFIED_REVERSE);
                displayFileOrder(files);
                if(fileCount==-1){
                    return;
                }
                int i=0;
                for (File file : files) {
                    i++;
                    if(i>fileCount){
                        file.delete();
                    }
                }
            }
        }catch(Exception e){
            log.error("",e);
        }
    }
    private void displayFileOrder(File[] files) {
        for (File file : files) {
//            System.out.printf("%2$td/%2$tm/%2$tY - %s%n", file.getName(),
//                    file.lastModified());
            log.info(file.getName()+" - "+(new Date(file.lastModified())));
        }
    }


    private void showDuration(Date startDate,Date endDate,String operation){
        try{
            long difference_In_Time = endDate.getTime() - startDate.getTime();
            long difference_In_Millis = (difference_In_Time % 1000);
            long difference_In_Seconds = (difference_In_Time / 1000)  % 60;

            long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;

            long difference_In_Hours = (difference_In_Time / (1000 * 60 * 60)) % 24;
            log.info("Duration Details for: "+operation);
            log.info("Start Time: "+startDate);
            log.info("End Time: "+endDate);
            log.info("Time Taken: "+difference_In_Hours +" hour(s) " + difference_In_Minutes + " minute(s) " + difference_In_Seconds+" second(s) "+difference_In_Millis+" millisecond(s)");

        }catch(Exception e){
            log.error("",e);
        }
    }
}
