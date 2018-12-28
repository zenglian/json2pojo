package liwey.json2pojo;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class GeneratorTest {

    @Test
    public void formatClassName() {
        assertEquals("Test", Generator.formatClassName("test"));
        assertEquals("Test2", Generator.formatClassName("test2"));
        assertEquals("TestWithUnderscores", Generator.formatClassName("test_with_underscores"));
        assertEquals("TestWithHyphens", Generator.formatClassName("test-with-hyphens"));
        assertEquals("Abstract", Generator.formatClassName("abstract"));
        assertEquals("AbstractTest", Generator.formatClassName("abstractTest"));
        assertEquals("Piñata", Generator.formatClassName("piñata"));
        assertEquals("Test", Generator.formatClassName("1Test"));
        assertEquals("InvalidChars", Generator.formatClassName("Invalid@$%@#$^&#%@Chars"));
    }

    @Test
    public void formatFieldNameWithoutMPrefix() {
        assertEquals("test", Generator.formatFieldName("test"));
        assertEquals("test2", Generator.formatFieldName("test2"));
        assertEquals("testWithUnderscores", Generator.formatFieldName("test_with_underscores"));
        assertEquals("testWithHyphens", Generator.formatFieldName("test-with-hyphens"));
        assertEquals("abstract", Generator.formatFieldName("abstract"));
        assertEquals("piñata", Generator.formatFieldName("piñata"));
        assertEquals("test", Generator.formatFieldName("1Test"));
        assertEquals("invalidChars", Generator.formatFieldName("Invalid@$%@#$^&#%@Chars"));
    }

    @Test
    public void testGenerateConfig() {
        File file = new File("d:/tmp/out");
        Generator generator = new Generator("test", file, null);
        String json = "{\n" +
                "  \"lombok\": true,\n" +
                "  \"lombok.annotations\": [\n" +
                "    {\n" +
                "      \"class.name\": \"lombok.Accessor\",\n" +
                "      \"params\": {\n" +
                "        \"fluent\": true\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"class.name\": \"lombok.Data\",\n" +
                "      \"params\": {}\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        generator.generateFromJson("test", json);
    }

    @Test
    public void testGenerateVersion() {
        File file = new File("d:/tmp/out");
        Generator generator = new Generator("test", file, null);
        String json = "{\n" +
                "\t  \"javaHome\": \"c:\\\\java18\",\n" +
                "\t  \"java.version\": \"1.8\",\n" +
                "\t  \"scala.version\": \"2.11.8\"\n" +
                "\t}";
        generator.generateFromJson("version", json);
    }
}