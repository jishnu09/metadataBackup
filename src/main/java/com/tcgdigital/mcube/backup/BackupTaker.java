package com.tcgdigital.mcube.backup;

import com.tcgdigital.mcube.backup.services.DatabaseBackupTaker;
import com.tcgdigital.mcube.backup.services.ElasticSearchDotAccountBackupTaker;
import com.tcgdigital.mcube.backup.utils.PropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class BackupTaker {

    private static final Logger log = LoggerFactory.getLogger(BackupTaker.class);

    public static void main(String arr[]) {
        try {
            AtomicReference<String> backupMode = new AtomicReference<>("cmd");
            Arrays.stream(arr).anyMatch(e -> {

                if (e.startsWith("--help")) {
                    printHelp();
                    System.exit(0);
                }
                if (e.startsWith("--mode=")) {
                    backupMode.set(e.split("--mode=")[1].trim());
                }

                return false;
            });

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
            final PropertyReader props = new PropertyReader();

            if (backupMode.get().equalsIgnoreCase("cmd")) {

                log.info("Backup Mode is CMD");

                props.setProperty("backup.elasticsearch.enabled", "N");
                props.setProperty("backup.database.enabled", "N");

                props.setProperty("backup.elasticsearch.xpack.enable", "");
                props.setProperty("backup.elasticsearch.accounts", "");
                props.setProperty("backup.elasticsearch.url", "");
                props.setProperty("backup.elasticsearch.username", "");
                props.setProperty("backup.elasticsearch.password", "");

                props.setProperty("backup.database.url", "");
                props.setProperty("backup.database.names", "");
                props.setProperty("backup.database.username", "");
                props.setProperty("backup.database.password", "");
                props.setProperty("backup.database.driver", "");


                Arrays.stream(arr).anyMatch(e -> {
                    System.out.println(e);
                    if (e.startsWith("--dBType=")) {
                        String dBType = e.split("--dBType=")[1].trim();
//                    System.out.println(dBType);
                        if (dBType.equalsIgnoreCase("elastic")) {
                            props.setProperty("backup.elasticsearch.enabled", "Y");
                            props.setProperty("backup.database.enabled", "N");
                        } else if (dBType.equalsIgnoreCase("database")) {
                            props.setProperty("backup.database.enabled", "Y");
                            props.setProperty("backup.elasticsearch.enabled", "N");
                        } else if (dBType.equalsIgnoreCase("both")) {
                            props.setProperty("backup.database.enabled", "Y");
                            props.setProperty("backup.elasticsearch.enabled", "Y");
                        } else {
                            printHelp();
                            throw new RuntimeException("Please enter database type[--dBType] either `elastic` or `database` or `both`");
                        }
                    }

                    if (e.startsWith("--esAccounts=")) {
                        String esAccounts = e.split("--esAccounts=")[1].trim();

                        props.setProperty("backup.elasticsearch.accounts", esAccounts);
                    }

                    if (e.startsWith("--esUrl=")) {
                        String esUrl = e.split("--esUrl=")[1].trim();
                        System.out.println(esUrl);
                        props.setProperty("backup.elasticsearch.url", esUrl);
                    }

                    if (e.startsWith("--xPackEnabled=")) {
                        String xPackEnabled = e.split("--xPackEnabled=")[1].trim();

                        props.setProperty("backup.elasticsearch.xpack.enable", xPackEnabled);
                    }

                    if (e.startsWith("--xPackUserName=")) {
                        String xPackUserName = e.split("--xPackUserName=")[1].trim();

                        props.setProperty("backup.elasticsearch.username", xPackUserName);
                    }

                    if (e.startsWith("--xPackPassword=")) {
                        String xPackPassword = e.split("--xPackPassword=")[1].trim();

                        props.setProperty("backup.elasticsearch.password", xPackPassword);
                    }

                    if (e.startsWith("--dbUrl=")) {
                        String dbUrl = e.split("--dbUrl=")[1].trim();
                        if (dbUrl.contains("_DATABASE_NAME_")) {
                            props.setProperty("backup.database.url", dbUrl);
                        } else {
                            printHelp();
                            throw new RuntimeException("Please enter database url[--dbUrl] should contain `_DATABASE_NAME_` wildcard in db URL");

                        }

                    }

                    if (e.startsWith("--dbUserName=")) {
                        String dbUserName = e.split("--dbUserName=")[1].trim();

                        props.setProperty("backup.database.username", dbUserName);
                    }

                    if (e.startsWith("--dbPassword=")) {
                        String dbPassword = e.split("--dbPassword=")[1].trim();

                        props.setProperty("backup.database.password", dbPassword);
                    }

                    if (e.startsWith("--dbNames=")) {
                        String dbNames = e.split("--dbNames=")[1].trim();

                        props.setProperty("backup.database.names", dbNames);
                    }

                    if (e.startsWith("--dbDriver=")) {
                        String dbDriver = e.split("--dbDriver=")[1].trim();

                        props.setProperty("backup.database.driver", dbDriver);
                    }

                    return false;
                });
            }else if (backupMode.get().equalsIgnoreCase("predefined")) {
                log.info("Backup Mode is Properties");
            }else{
                throw new RuntimeException("Invalid backup mode");
            }

            if (props.getProperty("backup.database.enabled").equalsIgnoreCase("Y")) {
                log.info("Database backup is enabled.........");
                DatabaseBackupTaker databaseBackupTaker = new DatabaseBackupTaker();
                databaseBackupTaker.takeBackup();
            } else {
                log.info("Database backup is disabled!!!!!!!!!!!!!!!!!!!!");
                log.info("To enable database backup set `backup.database.enabled` to `Y`");
            }
            if (props.getProperty("backup.elasticsearch.enabled").equalsIgnoreCase("Y")) {
                log.info("Elasticsearch backup is enabled.........");
                ElasticSearchDotAccountBackupTaker elasticSearchDotAccountBackupTaker = new ElasticSearchDotAccountBackupTaker();
                elasticSearchDotAccountBackupTaker.takeBackup();
            } else {
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
        } catch (Exception e) {
            log.error("", e);
            printHelp();
        }
    }

    private static void printHelp() {
        try {
            InputStream is = BackupTaker.class.getClassLoader().getResourceAsStream("help.txt");
            String str = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((str = reader.readLine()) != null) {
                    System.out.println(str);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
