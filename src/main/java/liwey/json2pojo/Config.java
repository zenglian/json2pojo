package liwey.json2pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author Leon Zeng
 * @since 2018/12/28 18:04
 */
@Data
public class Config {
    @JsonProperty("pri")
    private boolean primitive = true;
    private int fieldNameAnnotation = 0;
    private boolean lombokAccessors = false;
    private boolean lombokAccessorsFluent = true;
    private boolean lombokBuilder = false;
    private boolean lombokData = true;
    private boolean lombokNoArgsConstructor = false;
    private boolean lombokRequiredArgsConstructor = false;
    private boolean lombokAllArgsConstructor = false;

    public boolean useLombok() {
        return lombokBuilder || lombokData || lombokAccessors || lombokNoArgsConstructor
                || lombokRequiredArgsConstructor || lombokAllArgsConstructor;
    }
}
