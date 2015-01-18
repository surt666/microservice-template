(ns microservice-template.core-test
  (:require [expectations :refer :all]
            [microservice-template.core :refer :all]
            [ring.mock.request :as mock]))

(expect
 (dissoc (handler (mock/request :get "/service/12")) :headers)
 {:status  200
; :headers {"content-type" "text/plain"}
  :body    "{:foo \"bar\"}"})

(expect
 (dissoc (handler (mock/request :options "/service")) :headers)
 {:status  200
; :headers {"content-type" "text/plain"}
  :body    "{:foo \"bar\"}"})
