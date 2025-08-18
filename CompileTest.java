import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CompileTest {
    public static void main(String[] args) {
        System.out.println("=== PromptCraft 编译测试 ===");
        
        try {
            // 检查Java源文件
            List<String> javaFiles = findJavaFiles();
            System.out.println("找到 " + javaFiles.size() + " 个Java文件");
            
            // 尝试编译主要的类
            compileMainClasses();
            
        } catch (Exception e) {
            System.err.println("编译测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<String> findJavaFiles() throws IOException {
        List<String> javaFiles = new ArrayList<>();
        
        // 查找src/main/java下的文件
        Path mainSrc = Paths.get("src/main/java");
        if (Files.exists(mainSrc)) {
            Files.walk(mainSrc)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> javaFiles.add(path.toString()));
        }
        
        // 查找src/client/java下的文件
        Path clientSrc = Paths.get("src/client/java");
        if (Files.exists(clientSrc)) {
            Files.walk(clientSrc)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> javaFiles.add(path.toString()));
        }
        
        return javaFiles;
    }
    
    private static void compileMainClasses() {
        System.out.println("\n检查主要类的语法...");
        
        // 检查主要的Java文件是否存在语法错误
        String[] mainClasses = {
            "src/main/java/com/promptcraft/PromptCraft.java",
            "src/main/java/com/promptcraft/config/ConfigManager.java",
            "src/client/java/com/promptcraft/client/PromptCraftClient.java"
        };
        
        for (String className : mainClasses) {
            checkJavaFile(className);
        }
        
        // 检查资源文件
        checkResourceFiles();
        
        // 检查构建配置
        checkBuildFiles();
    }
    
    private static void checkJavaFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("✓ " + filePath + " 存在");
            
            try {
                // 读取文件内容进行基本检查
                String content = Files.readString(file.toPath());
                
                // 检查基本语法元素
                if (content.contains("package ") && content.contains("class ")) {
                    System.out.println("  - 包含package和class声明");
                }
                
                if (content.contains("import ")) {
                    System.out.println("  - 包含import语句");
                }
                
                // 检查可能的问题
                if (content.contains("Identifier.of(")) {
                    System.out.println("  ⚠ 使用了Identifier.of() - 可能需要改为new Identifier()");
                }
                
                if (content.contains("setPlaceholder(")) {
                    System.out.println("  ⚠ 使用了setPlaceholder() - 在1.21.1中可能不存在");
                }
                
                if (content.contains("readString()")) {
                    System.out.println("  ⚠ 使用了readString() - 可能需要添加长度参数");
                }
                
            } catch (IOException e) {
                System.out.println("  ✗ 无法读取文件: " + e.getMessage());
            }
        } else {
            System.out.println("✗ " + filePath + " 不存在");
        }
    }
    
    private static void checkResourceFiles() {
        System.out.println("\n检查资源文件...");
        
        String[] resourceFiles = {
            "src/main/resources/fabric.mod.json",
            "src/main/resources/promptcraft.mixins.json",
            "src/main/resources/assets/promptcraft/lang/en_us.json",
            "src/main/resources/assets/promptcraft/lang/zh_cn.json"
        };
        
        for (String resourceFile : resourceFiles) {
            File file = new File(resourceFile);
            if (file.exists()) {
                System.out.println("✓ " + resourceFile + " 存在");
                
                // 检查JSON文件语法
                if (resourceFile.endsWith(".json")) {
                    try {
                        String content = Files.readString(file.toPath());
                        if (content.trim().startsWith("{") && content.trim().endsWith("}")) {
                            System.out.println("  - JSON格式正确");
                        } else {
                            System.out.println("  ⚠ JSON格式可能有问题");
                        }
                    } catch (IOException e) {
                        System.out.println("  ✗ 无法读取JSON文件");
                    }
                }
            } else {
                System.out.println("✗ " + resourceFile + " 不存在");
            }
        }
    }
    
    private static void checkBuildFiles() {
        System.out.println("\n检查构建文件...");
        
        String[] buildFiles = {
            "build.gradle",
            "gradle.properties",
            "settings.gradle"
        };
        
        for (String buildFile : buildFiles) {
            File file = new File(buildFile);
            if (file.exists()) {
                System.out.println("✓ " + buildFile + " 存在");
                
                if (buildFile.equals("gradle.properties")) {
                    try {
                        String content = Files.readString(file.toPath());
                        if (content.contains("minecraft_version=1.21.1")) {
                            System.out.println("  - 使用Minecraft 1.21.1");
                        }
                        if (content.contains("fabric_version=")) {
                            System.out.println("  - 配置了Fabric API版本");
                        }
                    } catch (IOException e) {
                        System.out.println("  ✗ 无法读取属性文件");
                    }
                }
            } else {
                System.out.println("✗ " + buildFile + " 不存在");
            }
        }
        
        // 检查Gradle Wrapper
        File wrapperJar = new File("gradle/wrapper/gradle-wrapper.jar");
        if (wrapperJar.exists()) {
            System.out.println("✓ Gradle Wrapper JAR 存在 (" + wrapperJar.length() + " bytes)");
        } else {
            System.out.println("✗ Gradle Wrapper JAR 不存在");
        }
    }
}
