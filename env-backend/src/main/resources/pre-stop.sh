#!/usr/bin/env sh

xargs -rt -a /atp-environments/application.pid kill -SIGTERM
sleep 29
