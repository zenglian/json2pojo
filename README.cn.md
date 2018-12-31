[English README](README.md)

## 简介
IntelliJ Idea插件，从JSON文本生成POJO, 并添加Lombok与Gson/Jackson注解.

## 安装
从plugin库marketplace搜索`Json2Pojo with Lombok`。

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
      "primitive": true,
      "field.name.annotation": 1,
      "lombok.accessors": false,
      "lombok.accessors.fluent": true,
      "lombok.accessors.chain": true,
      "lombok.accessors.prefix": "",
      "lombok.builder": false,
      "lombok.data": true,
      "lombok.no.args.constructor": false,
      "lombok.required.args.constructor": true,
      "lombok.all.args.constructor": false
    }

