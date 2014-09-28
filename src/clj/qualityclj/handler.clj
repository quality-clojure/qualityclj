(ns qualityclj.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [qualityclj.models.db :as db]
            [qualityclj.routes.home :refer [home-routes]]))

(defn init []
  (db/ensure-db)
  (println "q2 is starting"))

(defn destroy []
  (println "q2 is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes app-routes)
      (handler/site)
      (wrap-base-url)))
