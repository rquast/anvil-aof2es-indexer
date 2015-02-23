#!/bin/bash

## To be executed on an ElasticSearch node after the following plugin is installed:
## https://github.com/salyh/elasticsearch-security-plugin

curl -XPUT 'http://localhost:9200/securityconfiguration/actionpathfilter/actionpathfilter' -d '
{
    "rules": [
        {
            "hosts" : [ "my.hostname.com" ],
            "permission" : "ALL"
        },
        {
            "permission" : "READONLY"
        }
    ]               
}'
