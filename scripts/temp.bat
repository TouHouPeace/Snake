@echo off
REM 设置 JavaFX 的路径，根据你本地 JavaFX 的安装路径进行调整
set JAVAFX_LIB="D:\APPJAVA\HW3T\wangz60\openjfx-22.0.1_windows-x64_bin-sdk\javafx-sdk-22.0.1\lib"

REM 创建输出目录（如果不存在）
if not exist ..\out (
    mkdir ..\out
)

REM 编译 Java 源文件
javac --module-path %JAVAFX_LIB% --add-modules javafx.controls -d ..\out ..\src\main\java\SnakeGameUI.java ..\src\main\java\SnakeGameLogic.java

REM 运行程序
java --module-path %JAVAFX_LIB% --add-modules javafx.controls -cp ..\out SnakeGameUI
