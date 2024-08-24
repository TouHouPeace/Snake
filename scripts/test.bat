@echo off
REM 设置 JavaFX 的路径
set JAVAFX_LIB="D:\APPJAVA\HW3T\wangz60\openjfx-22.0.1_windows-x64_bin-sdk\javafx-sdk-22.0.1\lib"

REM 创建输出目录（如果不存在）
if not exist ..\out (
    mkdir ..\out
)

REM 生成 Javadoc 文档，包含 JavaFX 库
javadoc -d ..\docs\manuals -private --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.graphics,javafx.fxml ..\src\main\java\SnakeGameUI.java ..\src\main\java\SnakeGameLogic.java
