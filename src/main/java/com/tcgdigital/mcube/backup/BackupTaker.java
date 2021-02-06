package com.tcgdigital.mcube.backup;

import com.tcgdigital.mcube.backup.services.DatabaseBackupTaker;
import com.tcgdigital.mcube.backup.services.ElasticSearchDotAccountBackupTaker;
import com.tcgdigital.mcube.backup.utils.PropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupTaker {

    private static final Logger log = LoggerFactory.getLogger(BackupTaker.class);

    public static void main(String arr[]){
        try{
            //amctubes
            System.out.println();
            System.out.println();
            System.out.println("d s   sb d sss   sss sssss d s.   d ss    d s.   sss sssss d s.        d ss.  d s.     sSSs. d     S d       b d ss.  \n" +
                    "S  S S S S           S     S  ~O  S   ~o  S  ~O      S     S  ~O       S    b S  ~O   S      S    P  S       S S    b \n" +
                    "S   S  S S           S     S   `b S     b S   `b     S     S   `b      S    P S   `b S       Ssss'   S       S S    P \n" +
                    "S      S S sSSs      S     S sSSO S     S S sSSO     S     S sSSO      S sSS' S sSSO S       S   s   S       S S sS'  \n" +
                    "S      S S           S     S    O S     P S    O     S     S    O      S    b S    O S       S    b  S       S S      \n" +
                    "S      S S           S     S    O S    S  S    O     S     S    O      S    P S    O  S      S     b  S     S  S      \n" +
                    "P      P P sSSss     P     P    P P ss\"   P    P     P     P    P      P `SS  P    P   \"sss' P     P   \"sss\"   P      ");
            System.out.println();
            System.out.println();
            final PropertyReader props=new PropertyReader();
            if(props.getProperty("backup.database.enabled").equalsIgnoreCase("Y")) {
                log.info("Database backup is enabled.........");
                DatabaseBackupTaker databaseBackupTaker = new DatabaseBackupTaker();
                databaseBackupTaker.takeBackup();
            }
            else{
                log.info("Database backup is disabled!!!!!!!!!!!!!!!!!!!!");
                log.info("To enable database backup set `backup.database.enabled` to `Y`");
            }
            if(props.getProperty("backup.elasticsearch.enabled").equalsIgnoreCase("Y")) {
                log.info("Elasticsearch backup is enabled.........");
                ElasticSearchDotAccountBackupTaker elasticSearchDotAccountBackupTaker = new ElasticSearchDotAccountBackupTaker();
                elasticSearchDotAccountBackupTaker.takeBackup();
            }else{
                log.info("Elasticsearch backup is disabled!!!!!!!!!!!!!!!!!!!!");
                log.info("To enable elasticsearch backup set `backup.elasticsearch.enabled` to `Y`");
            }
            Thread.sleep(2000);
            //banner3-D
            System.out.println();
            System.out.println();
            System.out.println("'########:'####:'##::: ##:'####::'######::'##::::'##:'########:'########:::::::::::::::::::::::::::::::::::::\n" +
                    " ##.....::. ##:: ###:: ##:. ##::'##... ##: ##:::: ##: ##.....:: ##.... ##::::::::::::::::::::::::::::::::::::\n" +
                    " ##:::::::: ##:: ####: ##:: ##:: ##:::..:: ##:::: ##: ##::::::: ##:::: ##::::::::::::::::::::::::::::::::::::\n" +
                    " ######:::: ##:: ## ## ##:: ##::. ######:: #########: ######::: ##:::: ##::::::::::::::::::::::::::::::::::::\n" +
                    " ##...::::: ##:: ##. ####:: ##:::..... ##: ##.... ##: ##...:::: ##:::: ##::::::::::::::::::::::::::::::::::::\n" +
                    " ##:::::::: ##:: ##:. ###:: ##::'##::: ##: ##:::: ##: ##::::::: ##:::: ##:'###:'###:'###:'###:'###:'###:'###:\n" +
                    " ##:::::::'####: ##::. ##:'####:. ######:: ##:::: ##: ########: ########:: ###: ###: ###: ###: ###: ###: ###:\n" +
                    "..::::::::....::..::::..::....:::......:::..:::::..::........::........:::...::...::...::...::...::...::...::");
            System.out.println();
            System.out.println();
        }catch(Exception e){
            log.error("",e);
        }
    }
}
