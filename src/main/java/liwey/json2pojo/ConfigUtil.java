package liwey.json2pojo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Leon Zeng
 * @since 2018/12/29 13:56
 */
public class ConfigUtil {
  private transient static final Path configPath = Paths.get(System.getProperty("user.home") + "/.json2pojo");
  private transient static final Gson gson = new GsonBuilder().setPrettyPrinting()
        .setFieldNamingStrategy(field -> separateCamelCase(field.getName(), ".").toLowerCase(Locale.ENGLISH))
        .create();

  public static final Config config = load();

  private static Config load() {
    try {
      return gson.fromJson(new String(Files.readAllBytes(configPath)), Config.class);
    } catch (Exception e) {
      return new Config();
    }
  }

  public static void save() throws IOException {
    Files.write(configPath, gson.toJson(config).getBytes());
  }

  /**
   * @see com.google.gson.FieldNamingPolicy
   */
  private static String separateCamelCase(String name, String separator) {
    StringBuilder translation = new StringBuilder();
    int i = 0;

    for (int length = name.length(); i < length; ++i) {
      char character = name.charAt(i);
      if (Character.isUpperCase(character) && translation.length() != 0) {
        translation.append(separator);
      }

      translation.append(character);
    }

    return translation.toString();
  }

  public static void setLocale() {
    String language = ConfigUtil.config.getLanguage().trim();
    if (language.startsWith("zh")) {
      Locale.setDefault(Locale.CHINESE);
    } else if (language.startsWith("en")) {
      Locale.setDefault(Locale.ENGLISH);
    }
  }
}
