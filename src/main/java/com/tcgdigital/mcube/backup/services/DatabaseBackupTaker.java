package com.tcgdigital.mcube.backup.services;

import com.smattme.MysqlExportService;
import com.tcgdigital.mcube.backup.utils.PropertyReader;
import com.tcgdigital.mcube.backup.utils.enc.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.Date;

import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;

public class DatabaseBackupTaker {
    private final Logger log = LoggerFactory.getLogger(DatabaseBackupTaker.class);

    private final PropertyReader props=new PropertyReader();

    public void takeBackup() throws Exception {
        try {
            log.info("Database Backup start..........");
            Date startTime= Calendar.getInstance().getTime();
            List<String> databaseList=Arrays.asList(props.getProperty("backup.database.names").split(","));
            if(databaseList.get(0).equals("*")){
                databaseList=fetchSchemasFromDatabase();
            }
            databaseList.forEach(database->{

                try {
                    Date startTime1= Calendar.getInstance().getTime();
                    Properties properties = new Properties();
                    String databaseUrl=props.getProperty("backup.database.url").replace("_DATABASE_NAME_",database);
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


//                    if (databaseUrl.contains("?")) {
//                        database = databaseUrl.substring(databaseUrl.lastIndexOf("/") + 1, databaseUrl.indexOf("?"));
//                    } else {
//                        database = databaseUrl.substring(databaseUrl.lastIndexOf("/") + 1);
//                    }
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
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<String> fetchSchemasFromDatabase() throws Exception{
        Connection con=null;
        List<String> schemaList = new LinkedList<>();
        try{
            Class.forName(props.getProperty("backup.database.driver"));
            String databaseUrl=props.getProperty("backup.database.url").replace("_DATABASE_NAME_","mysql");
            String userName = props.getProperty("backup.database.username");
            String passWord = props.getProperty("backup.database.password");

            con= DriverManager.getConnection(databaseUrl,userName,passWord);

            PreparedStatement ps = con.prepareStatement("SHOW DATABASES");
            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                if (!(rs.getString(1).equalsIgnoreCase("mysql")
                        ||rs.getString(1).equalsIgnoreCase("sys")
                        ||rs.getString(1).equalsIgnoreCase("performance_schema")
                        ||rs.getString(1).equalsIgnoreCase("information_schema"))){
                    schemaList.add(rs.getString(1));
                }
            }

        }catch(Exception e){
            log.error("", e);
            throw new RuntimeException(e.getMessage());
        }finally{
            if(null!=con){
                try {
                    con.close();
                } catch (SQLException e) {
                    log.error("", e);
                }
            }
        }
        return schemaList;
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
            log.info(String.format("%s - %s", file.getName(), new Date(file.lastModified())));
        }
    }


    private void showDuration(Date startDate,Date endDate,String operation){
        try{
            long differenceInTime = endDate.getTime() - startDate.getTime();
            long differenceInMillis = (differenceInTime % 1000);
            long differenceInSeconds = (differenceInTime / 1000)  % 60;

            long differenceInMinutes = (differenceInTime / (1000 * 60)) % 60;

            long differenceInHours = (differenceInTime / (1000 * 60 * 60)) % 24;
            log.info(String.format("Duration Details for: %s", operation));
            log.info(String.format("Start Time: %s", startDate));
            log.info(String.format("End Time: %s", endDate));
            log.info(String.format("Time Taken: %d hour(s) %d minute(s) %d second(s) %d millisecond(s)", differenceInHours, differenceInMinutes, differenceInSeconds, differenceInMillis));

        }catch(Exception e){
            log.error("",e);
        }
    }
}
