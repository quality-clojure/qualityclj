(ns qualityclj.models.db
  (:require [datomic.api :as d :refer [q db]]
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

(defn get-repos
  "Get a list of all repos in the database."
  []
  (map first (q '[:find ?repo
                  :where [_ :repo/uri ?repo]]
                (db @conn))))

(defn get-repo
  "Given the name of a repo, return a database entity representing the
  repository."
  [name]
  (let [id (q '[:find ?repo
                :in $ ?name
                :where
                [?repo :repo/uri ?uri]
                [(.contains ^String ?uri ^String ?name)]]
              (db @conn) name)]
    (d/entity (db @conn) (ffirst id))))
