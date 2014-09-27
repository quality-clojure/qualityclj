(ns qualityclj.db
  (:require [datomic.api :as d]))

(def uri (str "datomic:sql://quality-clj?jdbc:postgresql://"
              "localhost:5432/datomic?user=datomic&password=datomic"))
(def schema-tx (read-string (slurp "resources/data/schema.edn")))
(def fixtures (read-string (slurp "resources/data/initial.edn")))


(defn conn (d/connect uri))

(defn ensure-db []
  (when (d/create-database uri)
    (do
      (d/transact conn schema-tx)
      (d/transact conn fixtures))))
