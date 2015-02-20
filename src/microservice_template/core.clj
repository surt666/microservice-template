(ns microservice-template.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
;            [ring.middleware.multipart-params :only [wrap-multipart-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes ANY]]
            [liberator.dev :as dev]
            [liberator.representation :as rep]
            [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [cognitect.transit :as transit]
            [io.clojure.liberator-transit]
            [microservice-template.log :as log])
  (:import [java.net URL]))

(defn post-service [body]
  (prn "POST " body)
  [13])

(defn options-service [& [id]]
  (if id
    (str "med id " id)
    (str
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
     }")))

(defn put-service [id body]
  (prn "PUT " id " " body)
  [id])

(defn get-service [id]
  {"foo" "bar"})

(defn delete-service [id]
  (prn "DEL " id))

(defn build-entry-url [request & [id]]
  (if id
   (URL. (format "%s://%s:%s%s/%s"
                 (name (:scheme request))
                 (:server-name request)
                 (:server-port request)
                 (:uri request)
                 (str id)))
   (URL. (format "%s://%s:%s%s"
                 (name (:scheme request))
                 (:server-name request)
                 (:server-port request)
                 (:uri request)))))

;; convert the body to a reader. Useful for testing in the repl
;; where setting the body to a string is much simpler.
(defn body-as-string [ctx]
  (if-let [body (get-in ctx [:request :body])]
    (condp instance? body
      java.lang.String body
      (slurp (io/reader body)))))

;; For PUT and POST parse the body as json and store in the context
;; under the given key.
(defn parse-json [context key]
  (when (#{:put :post} (get-in context [:request :request-method]))
    (try
      (if-let [body (body-as-string context)]
        (let [data (parse-string body true)]
          [false {key data}])
        {:message "No body"})
      (catch Exception e
        (.printStackTrace e)
        {:message (format "IOException: " (.getMessage e))}))))

;; For PUT and POST check if the content type is json.
(defn check-content-type [ctx content-types]
  (if (#{:put :post} (get-in ctx [:request :request-method]))
    (or
     (some #{(get-in ctx [:request :headers "content-type"])}
           content-types)
     [false {:message "Unsupported Content-Type"}])
    true))

(defmethod rep/render-map-generic "application/vnd.skm+transit-json" [data context]
  (rep/render-map-generic data (assoc-in context [:representation :media-type] "application/transit+json")))

(defmethod rep/render-map-generic "application/vnd.skm+transit-msgpack" [data context]
  (rep/render-map-generic data (assoc-in context [:representation :media-type] "application/transit+msgpack")))

(defmethod rep/render-seq-generic "application/vnd.skm+transit-json" [data context]
  (rep/render-map-generic data (assoc-in context [:representation :media-type] "application/transit+json")))

(defmethod rep/render-seq-generic "application/vnd.skm+transit-msgpack" [data context]
  (rep/render-map-generic data (assoc-in context [:representation :media-type] "application/transit+msgpack")))

(defresource parameter [putfunc getfunc deletefunc optionsfunc & params]
  :available-media-types ["application/vnd.skm+transit-msgpack" "application/vnd.skm+transit-json" "application/clojure" "application/transit+json" "application/transit+msgpack"]
  :allowed-methods [:put :get :delete :options]
  :location #(build-entry-url (get % :request))
  :handle-ok (fn [ctx]
               (apply getfunc params))
  :handle-delete (fn [ctx]
                   (apply deletefunc params))
  :known-content-type? #(check-content-type % ["application/vnd.yousee.kasia2+json;charset=UTF-8"])
  :malformed? #(parse-json % ::data)
  :handle-put (fn [ctx]
                (let [body (get ctx ::data)
                      res (apply putfunc (conj params body))]
                  {::id (get res 0)}))
  :handle-options (fn [_] (apply optionsfunc params))
  :exists? (fn [ctx]
             (let [d (apply getfunc params)]
               (if (nil? d) false true)))
  :etag (fn [ctx]
          (hash (get ctx ::data))))

(defresource noparameter [postfunc optionsfunc]
  :available-media-types ["application/json" "application/vnd.skm+transit-json" "application/clojure"]
  :allowed-methods [:post :options]
  :known-content-type? #(check-content-type % ["application/json; charset=UTF-8" "application/vnd.skm+transit-json;charset=UTF-8"])
  :malformed? #(parse-json % ::data)
  :location #(build-entry-url (get % :request) (get % ::id))
  :handle-options (fn [_] (apply optionsfunc []))
  :post! (fn [ctx]
           (let [body (get ctx ::data)
                 res (apply postfunc [body])]
             {::id (get res 0)}))
  :etag (fn [ctx]
          (hash (get ctx ::data)))
  ;; :handle-created (fn [ctx] {:id (get ctx ::id)})
  )

(defroutes app
  (ANY "/service" [] (noparameter post-service options-service))
  (ANY "/service/:id" [id] (parameter put-service get-service delete-service options-service id))
  ;(ANY "/bar/:txt/:foo" [txt foo] (parameter call2 txt foo))
  )

(defn wrap-log [handler]
  (fn [req]
    (let [user (get-in req [:headers "x-user"])
          call-id (get-in req [:headers "x-callid"])]
      (log/info (str user " - " call-id " - " (:request-method req) " - " (:uri req) " " (:params req) " " (:body req))))
    (handler req)))

(def handler
  (-> app
      (wrap-log)
      (wrap-params)
      (dev/wrap-trace :ui :header)))

(defn start [options]
  (run-jetty #'handler (assoc options :join? false)))

(defn -main
  ([port]
     (start {:port (Integer/parseInt port)}))
  ([]
     (-main "8000")))
