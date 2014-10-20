(ns qualityclj.handler
  (:require [qualityclj.models.db :as db]
            [qualityclj.routes.home :refer [home-routes]]
            [qualityclj.routes.note :refer [note-routes]]
            [qualityclj.routes.repo :refer [repo-routes]]
            [compojure.core :refer [defroutes routes]]
            [compojure.route :as route]
            [hiccup.middleware :refer [wrap-base-url]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults
                                              api-defaults]]))

(defn init []
  (db/ensure-db))

(defn destroy [])

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

;; This is slightly counter-intuitive - the wrap-params middleware is
;; side-effecting, and causes issues down the line. Below, we delay
;; the param wrapping that happens in the api-defaults config until
;; the end, to prevent issues.
(def app
  (let [config (-> site-defaults
                   (dissoc [:params :urlencoded])
                   (dissoc [:params :keywordize]))
        api-routes (routes note-routes)
        site-routes (-> (routes home-routes repo-routes app-routes)
                        (wrap-defaults config))]
    (-> (routes api-routes site-routes)
        (wrap-defaults api-defaults)
        (wrap-base-url))))
