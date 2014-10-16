(ns qualityclj.models.db
  (:require [datomic.api :as d :refer [q db]]
            [clojure.java.io :as io]
            [environ.core :refer [env]]))

(def uri (env :db-uri))
(def schema-tx (read-string (slurp (io/resource "data/schema.edn"))))
(def fixtures (read-string (slurp (io/resource "data/initial.edn"))))

(defonce conn (atom nil))

(defn ensure-db []
  (let [new? (d/create-database uri)]
    (reset! conn (d/connect uri))
    (when new?
      @(d/transact @conn schema-tx)
      @(d/transact @conn fixtures))))

(defn file->entity
  "Given a File, convert it to the appropriate db entity ready to be
  added."
  [file]
  (let [file-id (d/tempid :db.part/user)]
    {:db/id file-id
     :file/name (.getName file)
     :file/path (.getPath file)}))

(defn import-repo
  "Import a git repository into the database."
  [uri user name files]
  (let [repo-id (d/tempid :db.part/user -2000)
        file-adds (mapv file->entity files)]
    @(d/transact @conn [{:db/id repo-id
                         :repo/uri uri
                         :repo/name name
                         :repo/username user
                         :repo/files file-adds}])))

(defn get-all-repos
  "Get a list of all repos in the database."
  []
  (q '[:find ?user ?project
       :where
       [?repo :repo/name ?project]
       [?repo :repo/username ?user]]
     (db @conn)))

(defn get-repo
  "Given the user and name of a repo, return a database entity id
  representing the repository."
  [user name]
  (let [id (q '[:find ?repo
                :in $ ?name ?user
                :where
                [?repo :repo/name ?name]
                [?repo :repo/username ?user]]
              (db @conn) name user)]
    (ffirst id)))

(defn remove-repo
  "Given a user/org name and a project name, remove all traces of a
  repo from the database."
  [user project]
  (let [repo-id (get-repo user project)]
    (if-not (nil? repo-id)
      @(d/transact @conn [[:db.fn/retractEntity repo-id]])
      (throw (IllegalArgumentException.
              (str "No repo " user "/" project " to delete."))))))

(defn source-files
  "Given a user and project name, return the analyzed files in the
  repository. For now, this is just the Clojure files."
  [user name]
  (let [repo (get-repo user name)]
    (if (nil? repo)
      #{}
      (sort (map first (q '[:find ?filepath
                            :in $ ?repo
                            :where
                            [?repo :repo/files ?files]
                            [?files :file/path ?filepath]]
                          (db @conn) repo))))))
