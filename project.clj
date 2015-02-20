(defproject microservice-template "0.1.0-SNAPSHOT"
  :description "Template for microservices"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [expectations "2.0.12"]
                 [liberator "0.12.2"]
                 [compojure "1.3.1"]
		 [ring/ring-core "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [cheshire "5.3.1"]
            ;     [javax.servlet/servlet-api "2.5"]
                 [com.cognitect/transit-clj "0.8.259"]
                 [ring/ring-mock "0.2.0"]
                 [io.clojure/liberator-transit "0.3.0"]
                 [ch.qos.logback/logback-classic "1.1.1"]]

  :plugins [[lein-expectations "0.0.7"]]

  ;;:exclusions [javax.servlet/servlet-api]

  ;; :profiles {:dev {:source-paths ["dev"]
  ;;                  :dependencies [[javax.servlet/servlet-api "2.5"]
  ;;                                 [org.eclipse.jetty/jetty-xml "7.6.1.v20120215"]
  ;;                                 [org.eclipse.jetty/jetty-webapp "7.6.1.v20120215"]
  ;;                                 [org.eclipse.jetty/jetty-plus "7.6.1.v20120215"]]}}
)
