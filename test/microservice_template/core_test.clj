(ns microservice-template.core-test
  (:require [expectations :refer :all]
            [microservice-template.core :refer :all]
            [ring.mock.request :as mock]))

(expect
 (let [m (dissoc (handler (mock/header (mock/request :get "/service/12") "accept" "application/vnd.skm+transit-json;verbose")) :headers)]
   (assoc m :body (slurp (:body m))))
 {:status  200
; :headers {"content-type" "text/plain"}
  :body    "{\"foo\":\"bar\"}"})

(expect
 (dissoc (handler (mock/header (mock/request :options "/service") "accept" "application/vnd.skm+transit-json")) :headers)
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
