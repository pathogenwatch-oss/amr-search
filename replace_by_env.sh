#!/bin/bash
output=$(cat $1)
while read -r line ; do

    proto="$(echo ${line} | grep :// | sed -e's,^\(.*://\).*,\1,g')"
    # remove the protocol
    url="$(echo ${line/$proto/})"
    # extract the user (if any)
    userpass="$(echo $url | grep @ | cut -d@ -f1)"
    pass="$(echo $userpass | grep : | cut -d: -f2)"
    if [ -n "$pass" ]; then
      user="$(echo $userpass | grep : | cut -d: -f1)"
    else
        user=$userpass
    fi

    # extract the host
    host="$(echo ${url/$user@/} | cut -d/ -f1)"
    # by request - try to extract the port
    port="$(echo $host | sed -e 's,^.*:,:,g' -e 's,.*:\([0-9]*\).*,\1,g' -e 's,[^0-9],,g')"
    # extract the path (if any)
    path="$(echo $url | grep / | cut -d/ -f2-)"

    # extract the domain
    domain="$(echo ${host} | sed 's/\(.*\):.*/\1/')"

#    echo "url: $url"
#    echo "  proto: $proto"
#    echo "  user: $user"
#    echo "  pass: $pass"
#    echo "  host: $host"
#    echo "  port: $port"
#    echo "  path: $path"
#    echo "  domain: $domain"

    output=$(echo "$output" | sed -e 's/\$'"HOST"'/'"$domain"'/')
    output=$(echo "$output" | sed -e 's/\$'"PROXY_PORT"'/'"$port"'/')
done
echo "$output"