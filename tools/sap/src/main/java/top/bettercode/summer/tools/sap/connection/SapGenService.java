package top.bettercode.summer.tools.sap.connection;

import com.sap.conn.jco.ConversionException;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import kotlin.io.FilesKt;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.generator.GeneratorExtension;
import top.bettercode.summer.tools.generator.dom.java.JavaType;
import top.bettercode.summer.tools.generator.dom.java.element.Field;
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility;
import top.bettercode.summer.tools.generator.dom.java.element.Method;
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass;
import top.bettercode.summer.tools.generator.dom.unit.SourceSet;
import top.bettercode.summer.tools.lang.operation.PrettyPrintingContentModifier;

/**
 * @author Peter Wu
 */
public class SapGenService {

  private final Logger log = LoggerFactory.getLogger(SapGenService.class);
  private final SapService sapService;
  private File outputDir = new File(System.getProperty("user.dir") + "/build/sap/");

  public SapGenService(SapService sapService) {
    this.sapService = sapService;
  }


  public SapGenService setOutputDir(@NotNull String outputDir) {
    Assert.notNull(outputDir, "outputDir must not be null");
    this.outputDir = new File(outputDir);
    return this;
  }

  public void gen(String pojoName, String functionName) throws JCoException, IOException {
    FilesKt.deleteRecursively(outputDir);
    JCoFunction function = sapService.getFunction(functionName);
    if (log.isInfoEnabled()) {
      log.info(PrettyPrintingContentModifier.modifyContent(function.toXML()));
    }
    JavaType classType = new JavaType(
        GeneratorExtension.javaName("SAP_" + pojoName + "_Service", true));
    TopLevelClass topLevelClass = new TopLevelClass(classType, true, SourceSet.ROOT,
        JavaVisibility.PUBLIC);

    Field field = new Field();
    field.setName(pojoName.toUpperCase(Locale.ROOT) + "_FUNCTION_NAME");
    field.setStatic(true);
    field.setFinal(true);
    field.setType(JavaType.Companion.getStringInstance());
    field.setInitializationString("\"" + functionName + "\"");
    field.setVisibility(JavaVisibility.PRIVATE);
    topLevelClass.field(field);

    field = new Field();
    topLevelClass.getImportedTypes().add(new JavaType("org.slf4j.LoggerFactory"));
    field.setInitializationString(
        "LoggerFactory.getLogger(" + classType.getShortName() + ".class)");
    field.setType(new JavaType("org.slf4j.Logger"));
    field.setName("log");
    field.setFinal(true);
    field.setVisibility(JavaVisibility.PRIVATE);
    topLevelClass.field(field);

    field = new Field();
    JavaType sapServiceType = new JavaType("top.bettercode.summer.tools.sap.connection.SapService");
    field.setType(sapServiceType);
    field.setName("sapService");
    field.setFinal(true);
    field.setVisibility(JavaVisibility.PRIVATE);
    topLevelClass.field(field);

    Method method = new Method();
    method.setConstructor(true);
    method.setName(classType.getShortName());
    method.setVisibility(JavaVisibility.PUBLIC);
    method.parameter(sapServiceType, "sapService");
    method.bodyLine("this.sapService = sapService;");
    topLevelClass.method(method);

    topLevelClass.writeTo(outputDir);

    JCoParameterList importParameterList = function.getImportParameterList();
    JCoParameterList exportParameterList = function.getExportParameterList();
    JCoParameterList tableParameterList = function.getTableParameterList();
    List<JCoField> inputTables = new ArrayList<>();
    if (importParameterList != null) {
      for (JCoField jCoField : importParameterList) {
        inputTables.add(jCoField);
      }
    }
    List<JCoField> outputTables = new ArrayList<>();
    if (exportParameterList != null) {
      for (JCoField jCoField : exportParameterList) {
        outputTables.add(jCoField);
      }
    }
    List<JCoField> tables = new ArrayList<>();
    if (tableParameterList != null) {
      for (JCoField jCoField : tableParameterList) {
        String description = jCoField.getDescription();
        String name = jCoField.getName();
        if (description.contains("输入") || name.startsWith("IT_")) {
          inputTables.add(jCoField);
        } else if (description.contains("输出") || name.startsWith("ET_")) {
          outputTables.add(jCoField);
        } else {
          tables.add(jCoField);
        }
      }
    }
    Properties properties = new Properties();
    genClass(pojoName, "Req", "", false, inputTables, properties);
    genClass(pojoName, "Resp", "", false, outputTables, properties);
    genClass(pojoName, CollectionUtils.isEmpty(outputTables) ? "Resp" : "Tables", "", false,
        tables, properties);
    File file = new File(outputDir, pojoName + ".properties");
    properties.store(Files.newBufferedWriter(file.toPath()), "SAP POJO properties");
    System.out.println("生成：" + file.getName());
  }

  private void genClass(String pojoName, String name, String desc, boolean exist,
      Iterable<JCoField> jCoFields, Properties properties) {
    if (jCoFields == null || !jCoFields.iterator().hasNext()) {
      return;
    }
    JavaType classType = new JavaType(
        (exist ? "pojo." : "") + GeneratorExtension.javaName(pojoName + "_" + name, true));
    TopLevelClass topLevelClass = new TopLevelClass(
        classType, true, SourceSet.ROOT, JavaVisibility.PUBLIC);
    boolean write = false;
    for (JCoField jCoField : jCoFields) {
      String jcoFieldName = jCoField.getName();
      String description = jCoField.getDescription();

      String fieldName = GeneratorExtension.javaName(jcoFieldName, false);
      properties.setProperty(fieldName, description);
      String annotation;
      String initializationString = null;
      JavaType type;
      JavaType javaType = new JavaType(
          GeneratorExtension.javaName(pojoName + "_" + jcoFieldName, true));
      if (jCoField.isStructure()) {
        annotation =
            "@top.bettercode.summer.tools.sap.annotation.SapStructure(\"" + jcoFieldName + "\")";
        switch (jcoFieldName) {
          case "IS_ZSCRM2_CONTROL":
            genClass(pojoName, jcoFieldName, description, true, jCoField.getStructure(),
                properties);
            type = new JavaType("top.bettercode.summer.tools.sap.connection.pojo.SapHead");
            fieldName = "head";
            break;
          case "ES_MESSAGE":
            genClass(pojoName, jcoFieldName, description, true, jCoField.getStructure(),
                properties);
            topLevelClass.superClass(new JavaType(
                "top.bettercode.summer.tools.sap.connection.pojo.SapReturn").typeArgument(
                "top.bettercode.summer.tools.sap.connection.pojo.RkEsMessage"));
            continue;
          default:
            type = javaType;
            genClass(pojoName, jcoFieldName, description, false, jCoField.getStructure(),
                properties);
            break;
        }
      } else if (jCoField.isTable()) {
        annotation =
            "@top.bettercode.summer.tools.sap.annotation.SapTable(\"" + jcoFieldName + "\")";
        if ("ET_RETURN".equals(jcoFieldName)) {
          topLevelClass.superClass("top.bettercode.summer.tools.sap.connection.pojo.EtReturns");
          genClass(pojoName, jcoFieldName, description, true, jCoField.getTable(), properties);
          continue;
        } else {
          type = new JavaType("java.util.List").typeArgument(javaType);
          genClass(pojoName, jcoFieldName, description, false, jCoField.getTable(), properties);
        }
      } else {
        annotation =
            "@top.bettercode.summer.tools.sap.annotation.SapField(\"" + jcoFieldName + "\")";
        Object value = null;
        try {
          value = jCoField.getValue();
        } catch (Exception ignored) {
        }
        int jCoFieldType = jCoField.getType();
        switch (jCoFieldType) {
          case 0:
          case 6:
          case 29:
            type = JavaType.Companion.getStringInstance();
            if (value != null && !"".equals(value)) {
              initializationString = "\"" + value + "\"";
            }
            break;
          case 1:
          case 3:
            type = JavaType.Companion.getDateInstance();
            break;
          case 2:
          case 23:
          case 24:
            type = new JavaType("java.math.BigDecimal");
            if (value != null) {
              initializationString = "new BigDecimal(\"" + value + "\")";
            }
            break;
          case 4:
          case 30:
            type = new JavaType("byte[]");
            break;
          case 7:
            type = new JavaType("java.lang.Double");
            break;
          case 8:
          case 9:
          case 10:
            type = new JavaType("java.lang.Integer");
            if (value != null) {
              initializationString = value.toString();
            }
            break;
          case 16:
            type = new JavaType("com.sap.conn.jco.rt.DefaultAbapObject");
            break;
          case 17:
            type = new JavaType("com.sap.conn.jco.rt.DefaultStructure");
            break;
          case 99:
            type = new JavaType("com.sap.conn.jco.rt.DefaultTable");
            break;
          default:
            throw new ConversionException(jcoFieldName + " unsupported type: " + jCoFieldType + "("
                + jCoField.getTypeAsString() + ")");
        }

      }
      write = true;
      Field field = new Field();
      field.setInitializationString(initializationString);
      field.annotation(annotation);
      field.setType(type);
      field.setName(fieldName);
      field.javadoc("/**"
          , " * " + description
          , " */");

      Method getMethod = new Method();
      getMethod.setVisibility(JavaVisibility.PUBLIC);
      getMethod.setName("get" + StringUtils.capitalize(fieldName));
      getMethod.javadoc("/**"
          , " * @return " + description
          , " */");
      getMethod.bodyLine("return this." + fieldName + ";");
      Method setMethod = new Method();
      setMethod.setVisibility(JavaVisibility.PUBLIC);
      setMethod.setName("set" + StringUtils.capitalize(fieldName));
      setMethod.javadoc("/**"
          , " * 设置" + description
          , " *"
          , " * @param " + fieldName + " " + description
          , " * @return " + (StringUtils.hasText(desc) ? desc : classType.getShortName())
          , " */");
      setMethod.bodyLine("this." + fieldName + " = " + fieldName + ";");
      setMethod.bodyLine("return this;");

      getMethod.setReturnType(type);
      setMethod.setReturnType(classType);
      setMethod.parameter(type, fieldName);

      field.setVisibility(JavaVisibility.PRIVATE);

      topLevelClass.field(field);
      topLevelClass.method(getMethod);
      topLevelClass.method(setMethod);
    }
    if (write) {
      Method toStringMethod = new Method();
      toStringMethod.setVisibility(JavaVisibility.PUBLIC);
      toStringMethod.setName("toString");
      toStringMethod.setReturnType(JavaType.Companion.getStringInstance());
      toStringMethod.annotation("@Override");
      toStringMethod.bodyLine("return StringUtil.json(this);");
      topLevelClass.getImportedTypes()
          .add(new JavaType("top.bettercode.summer.tools.lang.util.StringUtil"));
      topLevelClass.method(toStringMethod);
      topLevelClass.writeTo(outputDir);
    }
  }

}
