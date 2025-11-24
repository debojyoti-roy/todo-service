#!/bin/bash

echo "🧪 Running unit tests..."
./gradlew test

if [ $? -ne 0 ]; then
  echo "❌ Unit tests failed!"
  exit 1
else
  echo "✅ Unit tests passed successfully!"
fi

echo "🧹 Cleaning project and building the JAR..."
./gradlew clean bootJar

if [ $? -ne 0 ]; then
  echo "❌ Gradle build failed!"
  exit 1
fi

echo "🐳 Restarting containers with Docker Compose..."
docker-compose down
docker-compose up --build -d