(ns qualityclj.routes.repo
  (:require [qualityclj.models.db :as db]
            [qualityclj.views.layout :as layout]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.element :refer [link-to]])
  (:import java.io.File))

(defn list-repos
  "List the repos available to view."
  []
  (layout/common
   [:div.container-fluid
    [:h2 "Repositories"]
    [:ul#repos
     (for [repo (map #(string/join "/" %) (db/get-all-repos))]
       [:li.repo (link-to (str "repo/" repo) repo)])]]))

(defn show-repo
  "Show the files from a particular repo."
  [user repo]
  (let [files (db/source-files user repo)
        files (map #(string/replace % #"\\" "/") files)]
    (layout/common
     [:div.container-fluid
      [:h2 (str user "/" repo)]
      (if (empty? files)
        [:p "No such repository."]
        [:ul#files
         (for [file files]
           [:li.file (link-to (str "/repo/" file ".html") file)])])])))

(defn serve-file
  "Given a filepath, serve the highlighted file."
  [filepath]
  (let [highlight-filepath (str (db/highlight-path) File/separator filepath)
        file (io/file highlight-filepath)]
    (if (.exists file)
      (layout/common
       [:div.container-fluid
        [:h2#filepath (string/replace filepath #"\.html$" "")]
        [:div (slurp file)]
        [:div#notes]])
      (route/not-found (str "No such file: " file)))))

(defroutes repo-routes
  (context "/repo" []
           (GET "/" [] (list-repos))
           (GET "/:user/:repo" [user repo] (show-repo user repo))
           (GET "/*" request (serve-file (:* (:params request))))))
