(ns qualityclj.routes.home
  (:require [compojure.core :refer :all]
            [clygments.core :refer [highlight]]
            [qualityclj.views.layout :as layout]))

(def clygmatize (highlight "(def is-dev? (env :is-dev))\n\n(def inject-devmode-html\n (comp\n (set-attr :class \"is-dev\")\n (prepend (html [:script {:type \"text/javascript\" :src \"/out/goog/base.js\"}]))\n (prepend (html [:script {:type \"text/javascript\" :src \"/react/react.js\"}]))\n (append (html [:script {:type \"text/javascript\"} \"goog.require('qualityclj.core')\"]))))\n\n(defn browser-repl []\n (piggieback/cljs-repl :repl-env (weasel/repl-env :ip \"0.0.0.0\" :port 9001)))\n" :clojure :html))


(defn home []
  (layout/common [:div#source clygmatize]))

(defroutes home-routes
  (GET "/" [] (home)))
