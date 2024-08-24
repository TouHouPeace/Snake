#!/bin/bash

# Set the JavaFX path, adjust based on your local JavaFX installation path
JAVAFX_LIB="/Users/hanlin2004914/Downloads/javafx-sdk-22.0.1/lib"

# Create the output directory if it does not exist
if [ ! -d "../out" ]; then
    mkdir ../out
fi

# Compile the Java source files
javac --module-path "$JAVAFX_LIB" --add-modules javafx.controls -d ../out ../src/main/java/SnakeGameUI.java ../src/main/java/SnakeGameLogic.java

# Run the program
java --module-path "$JAVAFX_LIB" --add-modules javafx.controls -cp ../out SnakeGameUI
