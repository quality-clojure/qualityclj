(ns qualityclj.server
    (:require [clojure.java.io :as io]
              [qualityclj.dev :refer [is-dev? inject-devmode-html browser-repl]]
              [compojure.core :refer [GET defroutes]]
              [compojure.route :refer [resources]]
              [compojure.handler :refer [api]]
              [net.cgrand.enlive-html :refer [deftemplate]]
              [ring.middleware.reload :as reload]
              [environ.core :refer [env]]
              [ring.adapter.jetty :refer [run-jetty]]))

(deftemplate page
  (io/resource "index.html") [] [:body] (if is-dev? inject-devmode-html identity))

(defroutes routes
  (resources "/")
  (resources "/react" {:root "react"})
  (GET "/*" req (page)))

(def http-handler
  (if is-dev?
    (reload/wrap-reload (api #'routes))
    (api routes)))

(defn run [& [port]]
  (defonce ^:private server
    (run-jetty http-handler {:port (Integer. (or port (env :port) 10555))
                                 :join? false}))
  server)

(defn -main [& [port]]
  (run port))
