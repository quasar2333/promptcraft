#!/bin/bash

echo "Building PromptCraft mod..."

# Clean previous build
./gradlew clean

# Build the mod
./gradlew build

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo ""
    echo "Running tests..."
    
    # Run with test flag
    ./gradlew runClient -Dprompcraft.runTests=true
    
    echo ""
    echo "Build and test completed!"
    echo "Mod jar can be found in build/libs/"
else
    echo "Build failed!"
    exit 1
fi
