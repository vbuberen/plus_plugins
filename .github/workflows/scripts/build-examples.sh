#!/bin/bash

DEFAULT_TARGET="./test_driver/MELOS_PARENT_PACKAGE_NAME_e2e.dart"

JOB_NAME=$1
TARGET_FILE=${2:-$DEFAULT_TARGET}

melos bootstrap --scope="$PLUGIN_SCOPE"

if [ "$JOB_NAME" == "android" ]
then
  melos exec -c 1 --scope="$PLUGIN_EXAMPLE_SCOPE" -- \
    flutter build apk $FLUTTER_COMMAND_FLAGS --debug --target="$TARGET_FILE" --dart-define=CI=true --no-android-gradle-daemon
  MELOS_EXIT_CODE=$?
  pkill dart || true
  pkill java || true
  exit $MELOS_EXIT_CODE
fi

if [ "$JOB_NAME" == "ios" ]
then
  melos exec -c 1 --scope="$PLUGIN_EXAMPLE_SCOPE" -- \
    flutter build ios $FLUTTER_COMMAND_FLAGS --no-codesign --simulator --debug --target="$TARGET_FILE" --dart-define=CI=true
  exit
fi

if [ "$JOB_NAME" == "macos" ]
then
  melos exec -c 1 --scope="$PLUGIN_EXAMPLE_SCOPE" -- \
    flutter config --enable-macos-desktop && flutter build macos $FLUTTER_COMMAND_FLAGS --debug --target="$TARGET_FILE" --dart-define=CI=true
  exit
fi

if [ "$JOB_NAME" == "linux" ]
then
  melos exec -c 1 --scope="$PLUGIN_EXAMPLE_SCOPE" -- \
    flutter config --enable-linux-desktop && flutter build linux $FLUTTER_COMMAND_FLAGS --debug --target="$TARGET_FILE" --dart-define=CI=true
  exit
fi

if [ "$JOB_NAME" == "windows" ]
then
  melos exec -c 1 --scope="$PLUGIN_EXAMPLE_SCOPE" -- \
    flutter config --enable-windows-desktop && flutter build windows $FLUTTER_COMMAND_FLAGS --debug --target="$TARGET_FILE" --dart-define=CI=true
  exit
fi

if [ "$JOB_NAME" == "web" ]
then
  melos exec -c 1 --scope="$PLUGIN_EXAMPLE_SCOPE" -- \
    flutter build web $FLUTTER_COMMAND_FLAGS --debug --target="$TARGET_FILE" --dart-define=CI=true
  exit
fi
