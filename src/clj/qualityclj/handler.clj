(ns qualityclj.handler
  (:require [qualityclj.models.db :as db]
            [qualityclj.routes.home :refer [home-routes]]
            [qualityclj.routes.repo :refer [repo-routes]]
            [compojure.core :refer [defroutes routes]]
            [compojure.route :as route]
            [hiccup.middleware :refer [wrap-base-url]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.resource :refer [wrap-resource]]))

(defn init []
  (db/ensure-db))

(defn destroy [])

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes repo-routes app-routes)
      (wrap-defaults site-defaults)
      (wrap-base-url)))
