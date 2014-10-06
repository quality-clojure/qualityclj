(ns qualityclj.routes.file
  (:require [qualityclj.views.layout :as layout]
            [qualityclj.models.db :as db]
            [compojure.core :refer :all]
            [clojure.string :as string]
            [hiccup.element :refer [link-to]]))

(defn extract-user-repo
  "Given a URI for a git repo, extract the user/repo string."
  [uri]
  (let [splits (string/split uri #"[/:]")
        result (str (last (butlast splits)) "/" (last splits))]
    (string/replace result ".git" "")))

(defn list-repos
  "List the repos available to view."
  []
  (layout/common
   [:div.container-fluid
    [:h2 "Repositories"]
    [:ul#repos
     (for [repo (map extract-user-repo (db/get-repos))]
       [:li.repo (link-to (str "repo/" repo) repo)])]]))

(defroutes file-routes
  (context "/repo" []
    (GET "/" [] (list-repos))))
