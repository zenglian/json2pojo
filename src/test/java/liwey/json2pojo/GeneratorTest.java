package liwey.json2pojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.tools.*;

import org.junit.Assert;

import junit.framework.TestCase;

public class GeneratorTest extends TestCase {
    private static File srcFolder = null;
    private static File classesFolder = null;

    public void setUp() {
        if (srcFolder == null) {
            srcFolder = new File(System.getProperty("user.dir") + "/out");
            if (!srcFolder.exists())
                srcFolder.mkdirs();
        }

        if (classesFolder == null) {
            classesFolder = new File(System.getProperty("user.dir") + "/build/classes/java/main");
            if (!classesFolder.exists())
                classesFolder.mkdirs();
        }
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

    public void testGenerateConfig() throws IOException, ClassNotFoundException {
        String packageName = "package1";
        Generator generator = new Generator(packageName, srcFolder, null);
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
        generator.generateFromJson("Test", json);
        compile(packageName);
        Class test = Class.forName(packageName + ".Test");
        Assert.assertEquals(2, test.getDeclaredFields().length);
    }

    public void testNestedClass() throws IOException, ClassNotFoundException {
        String packageName = "package2";
        Generator generator = new Generator(packageName, srcFolder, null);
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
        generator.generateFromJson("Test", json);
        compile(packageName);
        Class test = Class.forName(packageName + ".Test");
        Assert.assertEquals(12, test.getDeclaredFields().length);
    }

    public void testGenerateNameAnnotation() throws IOException, ClassNotFoundException {
        String packageName = "package3";
        Generator generator = new Generator(packageName, srcFolder, null);
        String json = "{\n" +
                "\t  \"javaHome\": \"c:\\\\java18\",\n" +
                "\t  \"java.version\": \"1.8\",\n" +
                "\t  \"scala.version\": \"2.11.8\"\n" +
                "\t}";
        generator.generateFromJson("Test", json);
        compile(packageName);
        Class test = Class.forName(packageName + ".Test");
        Assert.assertEquals(3, test.getDeclaredFields().length);
    }

    private void compile(String packageName) throws IOException {
        List<String> classes = new ArrayList<>();
        for (File file : Objects.requireNonNull(new File(srcFolder.getPath() + "/" + packageName).listFiles())) {
            classes.add(file.getAbsolutePath());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        JavaFileManager.Location oLocation = StandardLocation.CLASS_OUTPUT;
        fileManager.setLocation(oLocation, Arrays.asList(classesFolder));
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(classes);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
        if (!task.call())
            throw new RuntimeException("Failed to compile generated classes: " + String.join(",", classes));
        fileManager.close();
    }
}