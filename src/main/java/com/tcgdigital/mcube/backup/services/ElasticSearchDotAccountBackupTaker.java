package com.tcgdigital.mcube.backup.services;

import com.tcgdigital.mcube.backup.utils.ElasticConnector;
import com.tcgdigital.mcube.backup.utils.PropertyReader;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;

public class ElasticSearchDotAccountBackupTaker {

    private final Logger log = LoggerFactory.getLogger(ElasticSearchDotAccountBackupTaker.class);

    private final PropertyReader props=new PropertyReader();

    private final DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");

    private final ElasticConnector elasticConnector =new ElasticConnector();

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public void takeBackup() {
        try{
            log.info("Elasticsearch Backup start..........");
            Date startTime= Calendar.getInstance().getTime();
            List<String> dotAccountsList=Arrays.asList(props.getProperty("backup.elasticsearch.accounts").split(","));
            String elasticsearchBackupPath=props.getProperty("backup.elasticsearch.path");
            File file=new File(elasticsearchBackupPath);
            if(!file.exists()){
                file.mkdirs();
            }
            if(dotAccountsList.get(0).equalsIgnoreCase("*")){
                log.info("Taking backup of all dot(.) account(s)");
                dotAccountsList=new ArrayList<>(getIndexListFromES());
            }
            dotAccountsList.forEach(dotAccount->{
                Date startTime1= Calendar.getInstance().getTime();
                log.info("Taking backup of account: "+dotAccount);
                JSONArray dotAccountData=getDotAccountData(dotAccount);
                if(!dotAccountData.isEmpty()){

                    FileWriter jsonFile = null;
                    try {
                        File tmpFile=new File(elasticsearchBackupPath+File.separator+dotAccount);
                        if(!tmpFile.exists()){
                            tmpFile.mkdirs();
                        }
                        LocalDateTime myDateObj = LocalDateTime.now();
                        String formattedDate = myDateObj.format(myFormatObj);
                        String dataFileName=formattedDate+"_"+(dotAccount).replace(".","")+"_data.json";
                        String mappingFileName=formattedDate+"_"+(dotAccount).replace(".","")+"_mapping.json";
                        String zipFileName=formattedDate+"_"+(dotAccount).replace(".","")+".zip";
                        jsonFile = new FileWriter(elasticsearchBackupPath+File.separator+dotAccount+File.separator+dataFileName);
                        jsonFile.write(dotAccountData.toString());
                        jsonFile.flush();
                        jsonFile.close();
                        jsonFile=null;


                        JSONObject mapping=getDotAccountMapping(dotAccount);

                        jsonFile = new FileWriter(elasticsearchBackupPath+File.separator+dotAccount+File.separator+mappingFileName);
                        jsonFile.write(mapping.toString());
                        jsonFile.flush();
                        jsonFile.close();
                        jsonFile=null;
                        String[] filesToZip=new String[2];
                        filesToZip[0]=elasticsearchBackupPath+File.separator+dotAccount+File.separator+dataFileName;
                        filesToZip[1]=elasticsearchBackupPath+File.separator+dotAccount+File.separator+mappingFileName;
                        zipFile(filesToZip,
                                elasticsearchBackupPath+File.separator+dotAccount+File.separator+zipFileName);

                        reconcileFiles(elasticsearchBackupPath+File.separator+dotAccount);
                        Date endTime= Calendar.getInstance().getTime();
                        showDuration(startTime1,endTime,dotAccount+" backup");
                    } catch (Exception e) {
                        log.error("",e);
                    }finally{
                        if(null!=jsonFile){
                            try {
                                jsonFile.close();
                            } catch (Exception e) {
                                log.error("",e);
                            }
                        }
                    }


                }
                log.info("[END]Taking backup of account: "+dotAccount);
            });
            Date endTime= Calendar.getInstance().getTime();
            showDuration(startTime,endTime," Full backup");
            log.info("..........Elasticsearch Backup end");
        }catch(Exception e){
            log.error("",e);
        }
    }
    private JSONArray getDotAccountData(String dotAccount){
        JSONArray dotAccountData=new JSONArray();
        try{
            OkHttpClient req = elasticConnector.connectElastic();
            String body="{\n" +
                    "    \"size\":100\n" +
                    "}";
            Request request = new Request.Builder()
                    .url(props.getProperty("backup.elasticsearch.url")+ dotAccount + "/_search?scroll=1m")
                    .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                    .build();

            Response response = req.newCall(request).execute();


            if(response.isSuccessful()){
                JSONObject resBody=new JSONObject(response.body().string());
                String scrollId=resBody.getString("_scroll_id");
                dotAccountData=parseResponseBody(resBody);
                if(!dotAccountData.isEmpty()){
                    while(true){
                        JSONArray scrollData=getScrollResponse(scrollId);
                        if(!scrollData.isEmpty()) {
                            dotAccountData=concatArray(dotAccountData,scrollData);
                        }else{
                            break;
                        }
                    }

                }
            }
        }catch(Exception e){
            log.error("",e);
        }
        return dotAccountData;
    }

    private JSONArray getScrollResponse(String scrollId) {
        JSONArray scrollData=new JSONArray();
        try{
            OkHttpClient req = elasticConnector.connectElastic();
            String body="{\n" +
                    "    \"scroll\": \"1m\",\n" +
                    "    \"scroll_id\": \"" + scrollId + "\"\n" +
                    "}";

            Request request = new Request.Builder()
                    .url(props.getProperty("backup.elasticsearch.url")+ "_search/scroll")
                    .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                    .build();

            Response response = req.newCall(request).execute();


            if(response.isSuccessful()){
                JSONObject resBody=new JSONObject(response.body().string());
                scrollData=parseResponseBody(resBody);
            }
        }catch(Exception e){
            log.error("",e);
        }
        return scrollData;
    }

    private JSONArray parseResponseBody(JSONObject resBody){
        JSONArray responseData=new JSONArray();
        try {
            if (resBody.has("hits")) {
                if (!resBody.isNull("hits")) {
                    JSONObject obj=resBody.getJSONObject("hits");
                    if(obj.has("hits")){
                        if (!obj.isNull("hits")) {
                            responseData=obj.getJSONArray("hits");
                        }
                    }
                }
            }

        }catch(Exception e){
            log.error("",e);
        }
        return responseData;
    }

    private JSONArray concatArray(JSONArray... arrs)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }
    private void reconcileFiles(String filePath){
        try{
            File dir = new File(filePath);
            File[] files = dir.listFiles();
            int fileCount= Integer.parseInt(props.getProperty("backup.elasticsearch.file.count"));
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
    private void zipFile(String[] sourceFileNames,String zipFileName){
        try{
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            Arrays.asList(sourceFileNames).forEach(sourceFileName->{
                zipFile(sourceFileName,zipOut);
            });
            zipOut.close();
            fos.close();
        }catch(Exception e){
            log.error("",e);
        }

    }
    private void zipFile(String sourceFileName,ZipOutputStream zipOut){
        try{
            File fileToZip = new File(sourceFileName);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            fileToZip.delete();
        }catch(Exception e){
            log.error("",e);
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

    private Set<String> getIndexListFromES() {
        Set<String> indicesList = new LinkedHashSet<String>();
        try {

            OkHttpClient req = elasticConnector.connectElastic();

            Request request = new Request.Builder()
                    .url(props.getProperty("backup.elasticsearch.url")+ "_aliases?pretty")
                    .build();

            Response response = req.newCall(request).execute();


            log.info("STATUS:" + response.code());
            log.info("STATUS TEXT:" + response.message());
            if (response.isSuccessful()) {
                JSONObject jResponse = new JSONObject(response.body().string());
                log.info(jResponse.toString());
                Set<String> keys = jResponse.keySet();
                keys.forEach(e -> {
                    if(e.startsWith(".")) {
                        if (!indicesList.contains(e)) {
                            indicesList.add(e);
                        }

                        if (!jResponse.getJSONObject(e).getJSONObject("aliases").isEmpty()) {
                            Set<String> aliasesKeys = jResponse.getJSONObject(e).getJSONObject("aliases").keySet();
                            aliasesKeys.forEach(alias -> {
                                if(alias.startsWith(".")) {
                                    if (!indicesList.contains(alias)) {
                                        indicesList.add(alias);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return indicesList;
    }

    private JSONObject getDotAccountMapping(String dotAccount) {
        JSONObject mapping=new JSONObject();
        try {

            OkHttpClient req = elasticConnector.connectElastic();

            Request request = new Request.Builder()
                    .url(props.getProperty("backup.elasticsearch.url")+ dotAccount+"/_mapping?pretty")
                    .build();

            Response response = req.newCall(request).execute();


            log.info("STATUS:" + response.code());
            log.info("STATUS TEXT:" + response.message());
            if (response.isSuccessful()) {
                mapping = new JSONObject(response.body().string());
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return mapping;
    }
}

