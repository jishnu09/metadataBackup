package com.tcgdigital.mcube.backup;

import com.tcgdigital.mcube.backup.services.DatabaseBackupTaker;
import com.tcgdigital.mcube.backup.services.ElasticSearchDotAccountBackupTaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupTaker {

    private static final Logger log = LoggerFactory.getLogger(BackupTaker.class);

    public static void main(String arr[]){
        try{
            DatabaseBackupTaker databaseBackupTaker=new DatabaseBackupTaker();
            databaseBackupTaker.takeBackup();
            ElasticSearchDotAccountBackupTaker elasticSearchDotAccountBackupTaker=new ElasticSearchDotAccountBackupTaker();
            elasticSearchDotAccountBackupTaker.takeBackup();
        }catch(Exception e){
            log.error("",e);
        }
    }
}
