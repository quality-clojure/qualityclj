(ns qualityclj.routes.home
  (:require [qualityclj.views.layout :as layout]
            [clojure.java.io :as io]
            [compojure.core :refer :all]
            [hiccup.form :refer [form-to label text-field
                                 submit-button hidden-field]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn home []
  (layout/common
   [:div.jumbotron
    [:h1 "Welcome to Quality Clojure!"]
    [:p "This is still a work in progress."]]
   [:div.container-fluid
    [:div.row
     [:div.col-md-8
      [:div#alert]]]
    [:div.row
     [:div.col-md-1
      (label "url-label" "Git URL")]
     [:div.col-md-4
      (text-field {:class "form-control"} "git-url" "")]
     [:div.col-md-3
      [:button#import-btn.btn.btn-primary "Import"]]]]))

(defn about []
  (layout/common
   [:div#header.container-fluid
    (seq (read-string (slurp (io/resource "static/about.edn"))))]))

(defroutes home-routes
  (GET "/" [] (home))
  (GET "/about" [] (about)))
