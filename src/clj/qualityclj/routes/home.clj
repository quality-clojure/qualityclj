(ns qualityclj.routes.home
  (:require [qualityclj.views.layout :as layout]
            [clojure.java.io :as io]
            [compojure.core :refer :all]))

(defn home []
  (layout/common
   [:div.jumbotron
    [:h1 "Welcome to Quality Clojure!"]
    [:p "This is still a work in progress."]]))

(defn about []
  (layout/common
   [:div#header.container-fluid
    (seq (read-string (slurp (io/resource "static/about.edn"))))]))

(defroutes home-routes
  (GET "/" [] (home))
  (GET "/about" [] (about)))
