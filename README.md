## 简介
IntelliJ Idea插件，从JSON文本生成POJO, 并添加Lombok与Gson/Jackson注解.

## 安装
从plugin库搜索`Json2Pojo with Lombok`。

## 使用
1. 右键目标package，选择"New-> Generate POJOs from JSON"  
![Context menu](image/menu.jpg "菜单")

2. 输入类名和源JSON文本。  
![Input UI](image/input.jpg "输入")

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

## 配置
![Settings UI](image/config.jpg "配置界面")

配置文件~/.json2pojo

    {
      "name.policy": "NONE",
      "lombok.accessors": true,
      "lombok.accessors.fluent": true,
      "lombok.builder": true,
      "lombok.data": true,
      "lombok.no.args.constructor": false,
      "lombok.required.args.constructor": false,
      "lombok.all.args.constructor": false
    }

