(ns qualityclj.routes.note
  (:require [qualityclj.models.db :as db]
            [cheshire.core :refer [generate-string]]
            [liberator.core :refer [defresource]]
            [compojure.core :refer [defroutes ANY context]]))

(defresource all-notes []
  :allowed-methods [:get]
  :available-media-types ["text/json"]
  :handle-ok (generate-string (db/get-all-notes)))

(defresource note []
  :allowed-methods [:get]
  :available-media-types ["text/json"]
  :exists? #(db/valid-filepath (:* (:params (:request %))))
  :handle-ok #(generate-string (db/get-notes (:* (:params (:request %)))))
  :handle-not-found "No such filepath.")

(defroutes note-routes
  (context "/note" []
    (ANY "/" [] (all-notes))
    (ANY "/*" [] (note))))
