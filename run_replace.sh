#!/bin/bash

env | grep -q proxy
if [ $? -eq 1 ]; then
  echo "Proxy settings not found"
  sed -i '/<proxies>/,/<\/proxies>/d' /root/.m2/settings.xml
else
  echo "Proxy settings found"
  cp /root/.m2/settings.xml /root/.m2/settings.template.xml
  env | grep http_proxy | /usr/local/bin/replace_by_env.sh  /root/.m2/settings.template.xml | sed '/\$PROXY_/d' | sed '/\$NO_PROXY/d' > /root/.m2/settings.xml
fi