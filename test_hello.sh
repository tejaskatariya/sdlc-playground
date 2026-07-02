#!/bin/bash
set -e

# Test script for hello.sh
# Validates output matches expected string exactly

expected="hello from the factory"
actual=$(./hello.sh)

if [ "$actual" = "$expected" ]; then
    echo "PASS: Output matches expected"
    exit 0
else
    echo "FAIL: Expected '$expected', got '$actual'"
    exit 1
fi
