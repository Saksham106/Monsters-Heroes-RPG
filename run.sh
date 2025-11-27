#!/bin/bash

# Run script for Monsters and Heroes RPG

# Check if compiled
if [ ! -d "bin" ]; then
    echo "Not compiled yet. Running compile script..."
    ./compile.sh
    echo ""
fi

echo "Starting Monsters and Heroes RPG..."
echo "===================================="
echo ""

java -cp bin game.Main


