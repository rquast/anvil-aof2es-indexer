# Redis AOF to ElasticSearch Indexer for Anvil Connect

## Description

The purpose of this daemon is to read append only files (Redis AOF mode) generated by Anvil Connect, and index the commands into ElasticSearch. This gives you the ability to search on the metadata of users, roles, clients and scopes, which is not (currently) provided by the rest/v1 routes in Anvil Connect.
