package com.tcgdigital.mcube.backup.utils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ElasticConnector
{

    private final Logger log= LoggerFactory.getLogger(ElasticConnector.class);
    private final PropertyReader props=new PropertyReader();

    public OkHttpClient connectElastic() {
        OkHttpClient elasticRequest =null;
        try {

            String username = props.getProperty("backup.elasticsearch.username");
            String password = props.getProperty("backup.elasticsearch.password");
            if(props.getProperty("backup.elasticsearch.xpack.enable").equalsIgnoreCase("1")) {
//                log.info("[ES] X-Pack enabled");
                elasticRequest = new OkHttpClient.Builder().authenticator(new Authenticator() {
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic(username, password);
                        return response.request().newBuilder().header("Authorization", credential).build();
                    }
                }).build();
            }else{
//                log.info("[ES] X-Pack disabled");
                elasticRequest=new OkHttpClient.Builder().build();
            }
        }catch(Exception e) {
            log.error("",e);
            elasticRequest=null;
        }
        return elasticRequest;
    }

}

