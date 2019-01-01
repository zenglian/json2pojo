package liwey.json2pojo;

import lombok.Data;

/**
 * @author Leon Zeng
 * @since 2018/12/28 18:04
 */
@Data
public class Config {
  private boolean fieldTypePrimitive = false;
  private int fieldNameAnnotation = 0;

  private boolean lombokAccessorsFluent = false;
  private boolean lombokAccessorsChain = false;
  private String lombokAccessorsPrefix = "";
  private boolean lombokBuilder = false;
  private boolean lombokData = true;
  private boolean lombokNoArgsConstructor = false;
  private boolean lombokRequiredArgsConstructor = true;
  private boolean lombokAllArgsConstructor = false;
  private String suppressWarnings = "unused";

  private String language = "";
  private int windowWidth = 500;
  private int windowHeight = 450;
  private int windowX = 100;
  private int windowY = 100;

  public boolean useAccessors() {
    return isLombokAccessorsFluent() || isLombokAccessorsChain()
          || !lombokAccessorsPrefix.trim().isEmpty();
  }
}
