(ns qualityclj.db
  (:require [datomic.api :as d]
            [clojure.java.io :as io]))

(def uri (str "datomic:mem://quality-clj"))
(def schema-tx (read-string (slurp (io/resource "data/schema.edn"))))
(def fixtures (read-string (slurp (io/resource "data/initial.edn"))))


(def conn (atom nil))

(defn ensure-db []
  (reset! conn (d/connect uri))
  (when (d/create-database uri)
    (do
      @(d/transact @conn schema-tx)
      @(d/transact @conn fixtures))))
