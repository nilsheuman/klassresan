#!/bin/bash

# reads all the projects files and appends together with files names for easily pasting into some llm

additional_files=(
  "settings.gradle.kts"
  "gradle.properties"
  "build.gradle.kts"
  "src/main/resources/META-INF/plugin.xml"
)

# Process all .h and .cpp files in src directory
for file in  src/main/java/se/snackesurf/intellij/klassresan/*.kt; do
  [ -f "$file" ] || continue
  filename=$(basename "$file")
  printf "\n\n// File: %s\n\n" "$filename"
  cat "$file"
  printf "\n\n"
done

# Process additional files
for file in "${additional_files[@]}"; do
  [ -f "$file" ] || continue
  filename=$(basename "$file")
  printf "\n\n// File: %s\n\n" "$filename"
  cat "$file"
  printf "\n"
done