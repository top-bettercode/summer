package cn.bestwu.summer.util.resourcesprocessor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import kotlin.text.Charsets;
import org.yaml.snakeyaml.Yaml;

@SupportedAnnotationTypes({"java.lang.Override"})
public class ResourcesAnnotationProcessor extends AbstractProcessor {


  private final Map<String, String> properties = new TreeMap<>();

  private static final String CONF_DIRECTORY = "conf";
  private static final String CLASSES_DIRECTORY = "classes";
  private static final Pattern TOKEN_PATTERN = Pattern.compile("@.+?@");

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    try {
      Filer filer = this.processingEnv.getFiler();
      FileObject file = filer.getResource(StandardLocation.CLASS_OUTPUT, "", "application.yml");

      File classPathDir = new File(file.toUri().toURL().getFile()).getParentFile();
      System.err.println("=======" + classPathDir.getAbsolutePath());
      String classPath = classPathDir.getAbsolutePath();
      int index = classPath.lastIndexOf(CLASSES_DIRECTORY);
      if (index < 0) {
        throw new FileNotFoundException();
      }
      String buildDirectoryPath = classPath.substring(0, index);
      File buildDirectory = new File(buildDirectoryPath);
      File configDir = findConfigDir(buildDirectory.getParentFile());
      if (configDir == null) {
        return false;
      }
      File rootConfigDir;
      try {
        rootConfigDir = new File(buildDirectory.getParentFile().getParentFile(), CONF_DIRECTORY);
        if (!rootConfigDir.exists() || rootConfigDir.isFile()) {
          rootConfigDir = configDir;
          configDir = null;
        }
      } catch (Exception e) {
        rootConfigDir = configDir;
        configDir = null;
      }
      if (rootConfigDir == null) {
        return false;
      }
      Properties properties = new Properties();
      loadProperties(rootConfigDir, configDir, properties);

      File[] files = classPathDir.listFiles();
      if (files != null) {
        for (File resource : files) {
          processor(resource, properties);
        }
      }
      File test = new File(classPathDir.getParentFile(), "test");
      if (test.exists()) {
        files = test.listFiles();
        if (files != null) {
          for (File resource : files) {
            processor(resource, properties);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private void processor(File resource, Properties properties) throws IOException {
    if (resource.isDirectory()) {
      for (File file : resource.listFiles()) {
        processor(file, properties);
      }
    } else {
      String name = resource.getName();
      int indexOf = name.lastIndexOf(".");
      if (indexOf == -1) {
        return;
      }
      if (name.substring(indexOf + 1) != "class") {
        FileOutputStream fileOutputStream = null;
        try {
          List<String> lines = new ArrayList<>();
          BufferedReader bufferedReader = new BufferedReader(
              new InputStreamReader(new FileInputStream(resource), Charsets.UTF_8));
          String line = bufferedReader.readLine();
          while (line != null) {
            String lstr = line;
            Matcher matcher = TOKEN_PATTERN.matcher(line);
            while (matcher.find()) {
              String group = matcher.group();
              group = group.substring(1, group.length() - 1);
              String property = properties.getProperty(group);
              if (property != null) {
                lstr = lstr.replace("@" + group + "@", property);
              }
            }
            lines.add(lstr);
            line = bufferedReader.readLine();
          }
          fileOutputStream = new FileOutputStream(resource);
          BufferedOutputStream buffer = new BufferedOutputStream(fileOutputStream);
          String ending = System.getProperty("line.separator");
          for (String l : lines) {
            if (l != null) {
              buffer.write(l.getBytes(StandardCharsets.UTF_8));
            }
            buffer.write(ending.getBytes(StandardCharsets.UTF_8));
          }
          buffer.flush();
          fileOutputStream.flush();
          fileOutputStream.close();
        } finally {
          if (fileOutputStream != null) {
            fileOutputStream.close();
          }
        }
      }
    }
  }

  private void loadProperties(File rootConfigDir, File configDir, Properties properties)
      throws IOException {
    String projectName;
    if (configDir == null) {
      projectName = rootConfigDir.getParentFile().getName();
    } else {
      projectName = configDir.getParentFile().getName();
    }
    properties.put("summer.web.project-name", projectName);

    File gradle = new File(rootConfigDir.getParentFile(), "gradle.properties");
    if (gradle.exists()) {
      properties.load(new FileInputStream(gradle));
    }
    File rootGradle = new File(System.getProperty("user.home"), ".gradle/gradle.properties");
    if (rootGradle.exists()) {
      properties.load(new FileInputStream(rootGradle));
    }
    String profilesActive = properties.getProperty("profiles.active");

    loadConfigProperties(rootConfigDir, profilesActive, properties);
    if (configDir != null) {
      loadConfigProperties(configDir, profilesActive, properties);
    }

    String packageName = properties.getProperty("app.packageName");
    if (packageName != null && packageName.length() > 0) {
      properties.put("app.packagePath", packageName.replace(".", "/"));
    }
  }

  private void loadConfigProperties(File configDir, String profilesActive,
      Properties properties) throws IOException {
    Yaml yaml = new Yaml();
    File defaultConfigYmlFile = new File(configDir, "default.yml");
    if (defaultConfigYmlFile.exists()) {
      Map<?, ?> load = yaml.load(new FileInputStream(defaultConfigYmlFile));
      properties.putAll(parseYml(load, new HashMap<>(), ""));
    }
    File activeYmlFile = new File(configDir, profilesActive + ".yml");
    if (activeYmlFile.exists()) {
      Map<?, ?> load = yaml.load(new FileInputStream(activeYmlFile));
      properties.putAll(parseYml(load, new HashMap<>(), ""));
    }

    defaultConfigYmlFile = new File(configDir, "default.yaml");
    if (defaultConfigYmlFile.exists()) {
      Map<?, ?> load = yaml.load(new FileInputStream(defaultConfigYmlFile));
      properties.putAll(parseYml(load, new HashMap<>(), ""));
    }
    activeYmlFile = new File(configDir, profilesActive + ".yaml");
    if (activeYmlFile.exists()) {
      Map<?, ?> load = yaml.load(new FileInputStream(activeYmlFile));
      properties.putAll(parseYml(load, new HashMap<>(), ""));
    }

    File defaultConfigFile = new File(configDir, "default.properties");
    if (defaultConfigFile.exists()) {
      properties.load(new FileInputStream(defaultConfigFile));
    }
    File activeFile = new File(configDir, profilesActive + ".properties");
    if (activeFile.exists()) {
      properties.load(new FileInputStream(activeFile));
    }
  }

  @SuppressWarnings("unchecked")
  private Map<Object, Object> parseYml(Map<?, ?> map, Map<Object, Object> result, String prefix) {
    for (Entry<?, ?> entry : map.entrySet()) {
      Object k = entry.getKey();
      Object u = entry.getValue();
      if (u != null) {
        if (u instanceof Map) {
          parseYml((Map<Object, Object>) u, result, prefix + k + ".");
        } else {
          result.put(prefix + k, u);
        }
      }
    }

    return result;
  }


  private File findConfigDir(File directoryPath) {
    if (directoryPath == null) {
      return null;
    }
    File config = new File(directoryPath, CONF_DIRECTORY);
    if (config.exists() && config.isDirectory()) {
      return config;
    } else {
      return findConfigDir(directoryPath.getParentFile());
    }
  }

}
