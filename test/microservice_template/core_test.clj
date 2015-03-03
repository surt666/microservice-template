(ns microservice-template.core-test
  (:require [expectations :refer :all]
            [microservice-template.core :refer :all]
            [ring.mock.request :as mock]))

(expect
 (let [req (mock/header
            (mock/header
             (mock/header
              (mock/request :get "/service/12")
              "accept" "application/vnd.skm+transit-json;verbose")
             "x-user" "sla")
            "x-callid" "983198379")
       res (dissoc (handler req) :headers)]
   (assoc res :body (slurp (:body res))))
 {:status  200
; :headers {"content-type" "text/plain"}
  :body    "{\"foo\":\"bar\",\"tal\":123.9939,\"dato\":\"~t2015-03-03T14:08:26.029Z\",\"link\":{\"url\":\"~rhttp://localhost:8080/service/4\",\"rel\":\"hent\"}}"})

(expect
 (let [req (mock/header
            (mock/header
             (mock/header
              (mock/request :options "/service")
              "accept" "application/vnd.skm+transit-json")
             "x-user" "sla")
            "x-callid" "983198379")]
   (dissoc (handler req) :headers))
 {:status  200
; :headers {"content-type" "text/plain"}
  :body    (str
"Method: See allow
Accept: See accept header
Content-Type: See accept header\n"
"Data in body should have the following format\n"
"    {
       \"billproduct/productid\": \"12334\",
       \"billproduct/name\": \"somename\",
       \"billproduct/rollover-date\": \"31-01-2015:12:00:00\",
       \"billproduct/rollover-product\": \"9393993\",
       \"billproduct/services\": [\"serv1\", \"serv2\"],
       \"billproduct/usage\": [\"usage1\"],
       \"billproduct/data\": [{\"billproduct.data/key\": \"somekey\", \"billproduct.data/value\": \"somevalue\"}]
     }")})
