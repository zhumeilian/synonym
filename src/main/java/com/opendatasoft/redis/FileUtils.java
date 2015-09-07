package com.opendatasoft.redis;

import com.opendatasoft.elasticsearch.index.config.SynonymsElasticConfigurator;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import java.io.*;
import java.util.regex.Pattern;

public class FileUtils {

    public static ESLogger logger = Loggers.getLogger("synonyms-redis-msg-file");

    public static void append(String content) {
        try {
            File file = new File(SynonymsElasticConfigurator.environment.configFile(), "ansj/synonyms.dic");
            appendFile(content, file);
        } catch (IOException e) {
            logger.error("read exception", e, new Object[0]);
            e.printStackTrace();
        }
    }

    private static void appendFile(String content, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.write(content);
        writer.newLine();
        writer.close();
    }

    public static void main(String[] args) {
        Pattern p = Pattern.compile("^满意\\D*$");
        System.out.println(p.matcher("满意  满      a       意      a").matches());
        System.out.println(p.matcher("满哈-满,意").matches());
        System.out.println("满哈-满,意".replace(",", "\t"));
    }


}
