![SKF Logo](https://os-hitask-dev2.s3.amazonaws.com/hh/skf/skf-logo-white2.png)

# Spring Boot application for communication with Redis #

## Parent ##
The app uses the last Spring Boot parent version: 2.4.5 that provides enough recent features of Redis Lists (indexOf, etc)

## Requirements ##
The app requires Redis server for both working and testing

It's possible to setup different configurations for working in /src/main/resources/application.properties and for testing
in /src/test/resources/application.properties

## REST Api ##

```
POST /api/v1/publish
GET /api/v1/getLast
GET /api/v1/getByTime
```

## How data is stored in Redis ###
Data is stored in Redis in list "contentTimes", value "contentLast" and hash "contentObjs"

### "contentTimes" list ####
It's used for indexing purposes. Every event stores it's publication time here. It has "1620478061773_0" form

Also system values like "1620478062000_label" are pushed into this list every second to find indexes during /getByTime
 
Every event has its own unique id in this list. _com.skf.rediscrud.service.CrudServiceImpl.putContent()_ has an explanation
how to get distributed unique value for this list

```
1620478061000_label
1620478061577_0
1620478061773_0
1620478061773_1
1620478061773_2
1620478062000_label
1620478062448_0
```
 
### "contentLast" value ####
It's used for storing the most recent event in Redis for /getLast endpoint

### "contentObjs" hash ####
"contentTimes" list contains hash keys of this hash map. So it's easy to retrieve event content by those keys

```
1620478061577_0 -> content_1620478061577
1620478061773_0 -> content_1620478061773
1620478061773_1 -> content_1620478061773
1620478061773_2 -> content_1620478061773
1620478062448_0 -> content_1620478062448
```

## Extra features ###

### Anti flood for GET requests ####
The app prevents more than 10 requests per second per remote ip

## Docker compose ##
Use /docker/docker-compose.yml to run the app. Change:
- volumes - to the folder with rediscrud-1.0.jar
- SKF_REDIS_SERVER - to point to Redis host