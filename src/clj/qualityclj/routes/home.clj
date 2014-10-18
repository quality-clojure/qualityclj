(ns qualityclj.routes.home
  (:require [qualityclj.views.layout :as layout]
            [qualityclj.import :as import]
            [clojure.java.io :as io]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [compojure.core :refer :all]
            [noir.response :as resp]
            [hiccup.form :refer [form-to label text-field
                                 submit-button hidden-field]]))

(defn home []
  (layout/common
   [:div.jumbotron
    [:h1 "Welcome to Quality Clojure!"]
    [:p "This is still a work in progress."]]
   [:div.container-fluid
    (form-to {:class "form-horizontal"} [:post "/import"]
             [:div.form-group.form-group-lg
              (anti-forgery-field)
              [:div.col-sm-2
               (label {:class "control-label"} "git-url" "Git URL")]
              [:div.col-sm-6
               (text-field {:placeholder "git: or https: URL"
                            :class "form-control"} "git-url")]
              [:div.col-sm-2
               (submit-button {:class "btn btn-default"} "Import")]])]))

(defn about []
  (layout/common
   [:div#header.container-fluid
    (seq (read-string (slurp (io/resource "static/about.edn"))))]))

(defn import-project
  "Start the import/update workflow."
  [url]
  (.start (Thread. #(import/import-or-update-project url)))
  (resp/redirect "/"))

(defroutes home-routes
  (GET "/" [] (home))
  (GET "/about" [] (about))
  (POST "/import" [git-url] (import-project git-url)))
