package liwey.json2pojo;

import java.io.File;
import java.util.Objects;

import org.junit.Assert;

import junit.framework.TestCase;

public class GeneratorTest extends TestCase {
    private static File out = null;

    public void setUp(){
        if(out == null) {
            out = new File("d:/tmp/src/main/java");
            if (!out.exists())
                out.mkdirs();

            for (File folder : Objects.requireNonNull(out.listFiles())) {
                if(folder.isDirectory()) {
                    for (File file : Objects.requireNonNull(folder.listFiles())) {
                        file.delete();
                    }
                }
            }
        }
    }

    private int getFileCount(String packageName){
        return Objects.requireNonNull(new File(out.getPath() + "/" + packageName).listFiles()).length;
    }

    public void testFormatClassName() {
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

    public void testFormatFieldNameWithoutMPrefix() {
        assertEquals("test", Generator.formatFieldName("test"));
        assertEquals("test2", Generator.formatFieldName("test2"));
        assertEquals("testWithUnderscores", Generator.formatFieldName("test_with_underscores"));
        assertEquals("testWithHyphens", Generator.formatFieldName("test-with-hyphens"));
        assertEquals("abstract", Generator.formatFieldName("abstract"));
        assertEquals("piñata", Generator.formatFieldName("piñata"));
        assertEquals("test", Generator.formatFieldName("1Test"));
        assertEquals("invalidChars", Generator.formatFieldName("Invalid@$%@#$^&#%@Chars"));
    }

    public void testGenerateConfig() {
        String packageName = "package1";
        Generator generator = new Generator(packageName, out, null);
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
        Assert.assertEquals(3, getFileCount(packageName));
    }

    public void testGenerateProgress() {
        String packageName = "package2";
        Generator generator = new Generator(packageName, out, null);
        String json = "{\n" +
                "  \"id\" : \"18092f4d-c54a-42c6-98e0-438aeef9f1fa\",\n" +
                "  \"runId\" : \"16a3734b-0418-4231-ae5f-c06ee8638f97\",\n" +
                "  \"name\" : \"Opportunity_UHIgcccj_MilestoneHistory\",\n" +
                "  \"timestamp\" : \"2018-11-10T11:18:20.005Z\",\n" +
                "  \"batchId\" : 55,\n" +
                "  \"numInputRows\" : 191,\n" +
                "  \"inputRowsPerSecond\" : 11.019442681590032,\n" +
                "  \"processedRowsPerSecond\" : 0.5130698500278025,\n" +
                "  \"durationMs\" : {\n" +
                "    \"addBatch\" : 371801,\n" +
                "    \"getBatch\" : 10,\n" +
                "    \"getOffset\" : 294,\n" +
                "    \"queryPlanning\" : 73,\n" +
                "    \"triggerExecution\" : 372269,\n" +
                "    \"walCommit\" : 86\n" +
                "  },\n" +
                "  \"stateOperators\" : [ {\n" +
                "    \"numRowsTotal\": 0,\n" +
                "    \"numRowsUpdated\": 0,\n" +
                "    \"memoryUsedBytes\": 0\n" +
                "  }],\n" +
                "  \"sources\" : [ {\n" +
                "    \"description\" : \"KafkaSource[Assign[insight-tenant1-0]]\",\n" +
                "    \"startOffset\" : {\n" +
                "      \"insight-tenant1\" : {\n" +
                "        \"a0\" : 234571\n" +
                "      }\n" +
                "    },\n" +
                "    \"endOffset\" : {\n" +
                "      \"insight-tenant1\" : {\n" +
                "        \"a0\" : 234762\n" +
                "      }\n" +
                "    },\n" +
                "    \"numInputRows\" : 191,\n" +
                "    \"inputRowsPerSecond\" : 11.019442681590032,\n" +
                "    \"processedRowsPerSecond\" : 0.5130698500278025\n" +
                "  } ],\n" +
                "  \"sink\" : {\n" +
                "    \"description\" : \"ForeachSink\"\n" +
                "  }\n" +
                "}";
        generator.generateFromJson("test", json);
        Assert.assertEquals(8, getFileCount(packageName));
    }

    public void testGenerateVersion() {
        String packageName = "package3";
        Generator generator = new Generator(packageName, out, null);
        String json = "{\n" +
                "\t  \"javaHome\": \"c:\\\\java18\",\n" +
                "\t  \"java.version\": \"1.8\",\n" +
                "\t  \"scala.version\": \"2.11.8\"\n" +
                "\t}";
        generator.generateFromJson("version", json);
        Assert.assertEquals(1, getFileCount(packageName));
    }
}