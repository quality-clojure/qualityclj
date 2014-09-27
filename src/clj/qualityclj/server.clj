(ns qualityclj.server
  (:require [qualityclj.db :as db]
            [qualityclj.dev :refer [is-dev? inject-devmode-html browser-repl]]
            [cemerick.piggieback :as piggieback]
            [clojure.java.io :as io]
            [compojure.core :refer [GET defroutes]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            [environ.core :refer [env]]
            [net.cgrand.enlive-html :as html :refer [deftemplate]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :as reload]
            [weasel.repl.websocket :as weasel])
  (:gen-class))

(defn init []
  (db/ensure-db))

(deftemplate page
  (io/resource "index.html") [] [:body] (if is-dev? inject-devmode-html identity))

(defroutes routes
  (resources "/")
  (resources "/react" {:root "react"})
  (GET "/*" req (page)))

(def http-handler
  (if is-dev?
    (reload/wrap-reload (site #'routes))
    (site routes)))

#_(defn -main [& [port]]
  (run port))
