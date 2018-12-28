package liwey.json2pojo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Leon Zeng
 * @since 2018/12/29 13:56
 */
public class ConfigUtil {
    private transient static final Path configPath = Paths.get(System.getProperty("user.home") + "/.json2pojo");
    private transient static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DOTS)
            .create();

    public static Config load() throws IOException {
        if(!Files.exists(configPath))
            save(new Config());

        return gson.fromJson(new String(Files.readAllBytes(configPath)), Config.class);
    }

    public static void save(Config config) throws IOException {
        Files.write(configPath, gson.toJson(config).getBytes());
    }
}
