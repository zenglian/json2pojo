package liwey.json2pojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.tools.*;

import org.junit.Assert;

import junit.framework.TestCase;

public class GeneratorTest extends TestCase {
    public void testFormatClassName() {
        assertEquals("Test", Generator.formatClassName("test"));
        assertEquals("TestCamelCase", Generator.formatClassName("testCamelCase"));
        assertEquals("TestWithUnderscores", Generator.formatClassName("test_with_underscores"));
        assertEquals("TestWithHyphens", Generator.formatClassName("test-with-hyphens"));
        assertEquals("TestWithDots", Generator.formatClassName("test.with.dots"));
        assertEquals("AbstractTest", Generator.formatClassName("abstractTest"));
        assertEquals("Test", Generator.formatClassName("1Test"));
        assertEquals("InvalidChars", Generator.formatClassName("Invalid@$%@#$^&#%@Chars"));
    }

    public void testExamples() throws Exception {
        String src = System.getProperty("user.dir") + "/src/test/java";
        File jsonFile = new File(src + "/example.json");
        String json = new String(Files.readAllBytes(jsonFile.toPath()));
        String dest = System.getProperty("user.dir") + "/build/classes/java/main";
        String packageName = "example.spark";
        Generator generator = new Generator(packageName, src, null);
        int n = generator.generateFromJson("SparkProgress", json);
        Assert.assertEquals(8, n);
    }

    private boolean compile(String packageName, String src, String dest) throws IOException {
        List<String> classes = new ArrayList<>();
        for (File file : Objects.requireNonNull(new File(src + "/" + packageName.replace('.', '/')).listFiles())) {
            classes.add(file.getAbsolutePath());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        JavaFileManager.Location oLocation = StandardLocation.CLASS_OUTPUT;
        fileManager.setLocation(oLocation, Arrays.asList(new File(dest)));
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(classes);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
        boolean result = task.call();
        fileManager.close();
        return result;
    }
}