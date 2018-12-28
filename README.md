## 简介
IntelliJ Idea插件，从Json文本生成POJO, 并添加Gson与Lombok注解.


## 使用方法
1. git clone本项目，在Intellij中build plugin。
2. 在IntelliJ中， Preferences -> Plugins -> Install from disk，选择build/distributions目录中的zip文件。
3. 在目标package, 选择New -> "Generate POJOs from JSON"。
4. 输入类名和源JSON文本。

## 配置
修改配置文件~/.json2pojo

    {
        "gson.serialized.name": true,
        "lombok": true,
        "lombok.annotations": [
            {
                "class.name": "lombok.Data"
            },
            {
                "class.name": "lombok.experimental.Accessors",
                "params": {
                    "fluent": "true"
                }
            }
        ]
    }


## 实例
输入Json:

	{
	  "javaHome": "c:\\java18",
	  "java.version": "12",
	  "scala.Version": "2.12.8"
	}

生成类：


    package test;

    import com.google.gson.annotations.SerializedName;
    import lombok.Data;
    import lombok.experimental.Accessors;

    @Data
    @Accessors(fluent = true)
    @SuppressWarnings("unused")
    public class Version {
        private String javaHome;
        @SerializedName("java.version")
        private String javaVersion;
        @SerializedName("scala.version")
        private String scalaVersion;
    }
