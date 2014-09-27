(ns qualityclj.db
  (:require [datomic.api :as d]
            [clojure.java.io :as io]))

(def uri (str "datomic:free://localhost:4334/qualityclj"))
(def schema-tx (read-string (slurp (io/resource "data/schema.edn"))))
(def fixtures (read-string (slurp (io/resource "data/initial.edn"))))

(defonce conn (atom nil))

(defn ensure-db []
  (let [new? (d/create-database uri)]
    (reset! conn (d/connect uri))
    (when new?
      (do
        @(d/transact @conn schema-tx)
        @(d/transact @conn fixtures)))))

