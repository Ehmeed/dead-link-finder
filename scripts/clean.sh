#!/usr/bin/env bash
ps a | grep "scripts/server.py" | grep -v "grep" | awk '{print $1}' | xargs kill -9
