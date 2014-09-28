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
            [weasel.repl.websocket :as weasel]
            [clygments.core :refer [highlight]]))

(defn clygmatize [] (highlight "(def is-dev? (env :is-dev))\n\n(def inject-devmode-html\n  (comp\n     (set-attr :class \"is-dev\")\n     (prepend (html [:script {:type \"text/javascript\" :src \"/out/goog/base.js\"}]))\n     (prepend (html [:script {:type \"text/javascript\" :src \"/react/react.js\"}]))\n     (append  (html [:script {:type \"text/javascript\"} \"goog.require('qualityclj.core')\"]))))\n\n(defn browser-repl []\n  (piggieback/cljs-repl :repl-env (weasel/repl-env :ip \"0.0.0.0\" :port 9001)))\n" :clojure :html))

(defn init []
  (db/ensure-db))

(deftemplate page
  (io/resource "index.html") [] [:body] (if is-dev? inject-devmode-html identity))

(deftemplate source-file
  (io/resource "index.html") [] [:body] (html/html-content (clygmatize)))

(defroutes routes
  (resources "/")
  (resources "/react" {:root "react"})
  (resources "/css" {:root "css"})
;  (GET "/*" req (page))
  (GET "/*" req (source-file)))

(def http-handler
  (if is-dev?
    (reload/wrap-reload (site #'routes))
    (site routes)))
