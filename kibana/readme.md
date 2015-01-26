
## TODO

Components
- Dateformat / Heatmap OK
- Sentiment OK
- SrcName -> Terms -> Extra feld

- NER: Terms -> _
- Locations


## NOTES

curl -XDELETE http://dbpro-2014ws.dima.tu-berlin.de:443/newsprocessed/article/*;

Mapping

curl -XDELETE http://localhost:9200/a1;
curl -XPUT 'http://localhost:9200/a1' -d '{
    "mappings": {"article": {
        "properties": {
            "srcName": {
                "type":"string",
                "index":"not_analyzed"
                },
            "timeSlot": {
                "type": "string",
                "index": "not_analyzed"
                },
            "ner": {
                "properties": {
                    "I-LOC": {
                        "type": "string",
                        "index": "not_analyzed"
                    },
                    "I-MISC": {
                        "type": "string",
                        "index": "not_analyzed"
                    },
                    "I-ORG": {
                        "type": "string",
                        "index": "not_analyzed"
                    },
                    "I-PER": {
                        "type": "string",
                        "index": "not_analyzed"
                    }
                }
            }
        }
    }
}}'



curl -XDELETE http://localhost:9200/a1;
curl -XPUT 'http://dbpro-2014ws.dima.tu-berlin.de:443/newsprocessed' -d '{
    "mappings": {"article": {
        "properties": {
            "srcName": {
                "type":"string",
                "index":"not_analyzed"
                },
            "timeSlot": {
                "type": "string",
                "index": "not_analyzed"
                },
            "ner": {
                "properties": {
                    "I-LOC": {
                        "type": "string",
                        "index": "not_analyzed"
                    },
                    "I-MISC": {
                        "type": "string",
                        "index": "not_analyzed"
                    },
                    "I-ORG": {
                        "type": "string",
                        "index": "not_analyzed"
                    },
                    "I-PER": {
                        "type": "string",
                        "index": "not_analyzed"
                    }
                }
            }
        }
    }
}}'

curl -XPOST 'http://localhost:9200/l1/article' -d '{
    "title": "foo",
    "countryCode": "RU"
}'

curl -XPOST 'http://localhost:9200/l2/article' -d '{
    "title": "foo",
    "countryCodes": ["FR", "BR", "US", "ML"]
}'

curl -XPOST 'http://localhost:9200/l3/article' -d '{
    "title": "foo",
    "lnglat": [105.318756, 61.52401]
}'

curl -XPOST 'http://localhost:9200/l3/article' -d '{
    "title": "foo",
    "lnglat": [13.40, 52.52]
}'

curl -XPOST 'http://localhost:9200/l3/article' -d '{
    "title": "foo X",
    "lnglats": [
        [13.40, 52.52],
        [14.40, 52.52],
        [13.40, 50.52]
        ]
}'

curl -XPOST 'http://localhost:9200/l3/article' -d '{
    "title": "foo X",
    "locations": [
        {"lng": 13.40, "lat": 52.52},
        {"lng": 13.90, "lat": 51.52},
        {"lng": 4.40, "lat": 92.52}
        ]
}'

curl -XPOST 'http://localhost:9200/l3/article' -d '{
    "title": "foo Y",
    "locations": [
        {"lng": 13.40, "lat": 82.52},
        {"lng": 18.90, "lat": 31.52},
        {"lng": 29.40, "lat": 92.52}
        ]
}'

curl -XPUT 'http://localhost:9200/dbpro/locations/_mapping' -d '{"locations": {"properties":{"location":{"type":"geo_point"}}}}'

curl -XGET 'http://localhost:9200/_all/_search?pretty' -d '{
  "query": {
    "filtered": {
      "query": {
        "bool": {
          "should": [
            {
              "query_string": {
                "query": "*"
              }
            }
          ]
        }
      },
      "filter": {
        "bool": {
          "must": [
            {
              "match_all": {}
            },
            {
              "exists": {
                "field": "locations"
              }
            }
          ]
        }
      }
    }
  },
  "fields": [
    "locations.lat",
    "locations.lng",

    "title"
  ],
  "size": 1000
}'

#####

PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT ?country ?lat ?long WHERE {
    ?country a dbo:Country ; foaf:name "Germany"@en;  geo:lat ?lat ; geo:long ?long
}
LIMIT 1

###

PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT * WHERE {
?s a dbo:Place ; foaf:name "Berlin"@en.
?s geo:lat ?lat .
?s geo:long ?long .
}
LIMIT 10

###

city dump

cut -f 2,4,5,6,9

0   1 name  2 localname 3 alias
3039154 El Tarter       El Tarter       Ehl Tarter,Эл Тартер    42.57952        1.65362 P       PPL     AD              02                              1052            1721    Europe/Andorra  2012-11-03
3039163 Sant Julià de Lòria     Sant Julia de Loria     San Julia,San Julià,Sant Julia de Loria,Sant Julià de Lòria,Sant-Zhulija-de-Lorija,sheng hu li ya-de luo li ya,Сант-Жулия-де-Лория,サン・ジュリア・デ・ロリア教区,圣胡利娅-德洛里亚,圣胡利娅－德洛里亚  42.46372        1.49129 P       PPLA    AD              06                              8022            921     Europe/Andorra  2013-11-23
3039604 Pas de la Casa  Pas de la Casa  Pas de la Kasa,Пас де ла Каса   42.54277        1.73361 P       PPL     AD              03                              2363    2050    2106    Europe/Andorra  2008-06-09
3039678 Ordino  Ordino  Ordino,ao er di nuo,orudino jiao qu,Ордино,オルディノ教区,奥尔迪诺      42.55623        1.53319 P       PPLA    AD              05                              3066            1296    Europe/Andorra  2009-12-11
