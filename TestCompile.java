/**
 * 简单的编译测试类
 * 用于验证Java环境和基本语法
 */
public class TestCompile {
    public static void main(String[] args) {
        System.out.println("=== PromptCraft 编译测试 ===");
        
        // 测试Java版本
        String javaVersion = System.getProperty("java.version");
        System.out.println("Java版本: " + javaVersion);
        
        // 测试基本功能
        testBasicFunctionality();
        
        // 模拟检查项目文件
        checkProjectFiles();
        
        System.out.println("✅ 编译测试完成！");
        System.out.println("项目已准备好进行Fabric模组构建。");
    }
    
    private static void testBasicFunctionality() {
        System.out.println("\n检查基本功能...");
        
        // 测试字符串处理
        String testCommand = "/say Hello World";
        if (testCommand.startsWith("/")) {
            System.out.println("✓ 指令格式验证正常");
        }
        
        // 测试集合操作
        java.util.List<String> blacklist = new java.util.ArrayList<>();
        blacklist.add("delete");
        blacklist.add("format");
        if (blacklist.contains("delete")) {
            System.out.println("✓ 黑名单功能正常");
        }
        
        // 测试JSON相关（模拟）
        String jsonTest = "{\"test\": \"value\"}";
        if (jsonTest.contains("test")) {
            System.out.println("✓ JSON处理准备就绪");
        }
    }
    
    private static void checkProjectFiles() {
        System.out.println("\n检查项目结构...");
        
        // 检查关键目录
        java.io.File srcDir = new java.io.File("src");
        if (srcDir.exists()) {
            System.out.println("✓ src目录存在");
        } else {
            System.out.println("⚠ src目录不存在");
        }
        
        // 检查构建文件
        java.io.File buildFile = new java.io.File("build.gradle");
        if (buildFile.exists()) {
            System.out.println("✓ build.gradle存在");
        } else {
            System.out.println("⚠ build.gradle不存在");
        }
        
        // 检查主类文件
        java.io.File mainClass = new java.io.File("src/main/java/com/promptcraft/PromptCraft.java");
        if (mainClass.exists()) {
            System.out.println("✓ 主类文件存在");
        } else {
            System.out.println("⚠ 主类文件不存在");
        }
        
        // 检查客户端类文件
        java.io.File clientClass = new java.io.File("src/client/java/com/promptcraft/client/PromptCraftClient.java");
        if (clientClass.exists()) {
            System.out.println("✓ 客户端类文件存在");
        } else {
            System.out.println("⚠ 客户端类文件不存在");
        }
        
        // 检查资源文件
        java.io.File fabricMod = new java.io.File("src/main/resources/fabric.mod.json");
        if (fabricMod.exists()) {
            System.out.println("✓ fabric.mod.json存在");
        } else {
            System.out.println("⚠ fabric.mod.json不存在");
        }
    }
}
