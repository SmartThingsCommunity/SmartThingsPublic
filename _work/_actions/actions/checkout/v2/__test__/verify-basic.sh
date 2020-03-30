#!/bin/sh

if [ ! -f "./basic/basic-file.txt" ]; then
    echo "Expected basic file does not exist"
    exit 1
fi

if [ "$1" = "--archive" ]; then
  # Verify no .git folder
  if [ -d "./basic/.git" ]; then
    echo "Did not expect ./basic/.git folder to exist"
    exit 1
  fi
else
  # Verify .git folder
  if [ ! -d "./basic/.git" ]; then
    echo "Expected ./basic/.git folder to exist"
    exit 1
  fi

  # Verify auth token
  cd basic
  git fetch --no-tags --depth=1 origin +refs/heads/master:refs/remotes/origin/master
fi
