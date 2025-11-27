#!/bin/bash

# Compilation script for Monsters and Heroes RPG

echo "Compiling Monsters and Heroes RPG..."

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files
find src -name "*.java" > sources.txt
javac -d bin @sources.txt

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    echo "To run the game:"
    echo "  java -cp bin game.Main"
    echo ""
    echo "Or use: ./run.sh"
else
    echo "✗ Compilation failed!"
    exit 1
fi

# Clean up
rm sources.txt


