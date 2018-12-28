package liwey.json2pojo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Leon Zeng
 * @since 2018/12/28 18:04
 */
@Data
@Accessors(fluent = true)
public class Config {
    private NamePolicy namePolicy = NamePolicy.NONE;
    private boolean lombokAccessors = false;
    private boolean lombokAccessorsFluent = true;
    private boolean lombokBuilder = true;
    private boolean lombokData = true;
    private boolean lombokNoArgsConstructor = false;
    private boolean lombokRequiredArgsConstructor = false;
    private boolean lombokAllArgsConstructor = false;
    public boolean useLombok(){
        return lombokBuilder || lombokData || lombokAccessors || lombokNoArgsConstructor
                || lombokRequiredArgsConstructor || lombokAllArgsConstructor;
    }

    enum NamePolicy {
        NONE,
        GSON,
        JACKSON
    }
}
