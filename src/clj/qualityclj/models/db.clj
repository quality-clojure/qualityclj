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
  (let [repo-id (d/tempid :db.part/user)
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
  (ffirst (q '[:find ?repo
               :in $ ?name ?user
               :where
               [?repo :repo/name ?name]
               [?repo :repo/username ?user]]
             (db @conn) name user)))

(defn remove-repo
  "Given a user/org name and a project name, remove all traces of a
  repo from the database. Idempotent - removing a nonexistent repo
  does nothing."
  [user project]
  (let [repo-id (get-repo user project)]
    (when-not (nil? repo-id)
      @(d/transact @conn [[:db.fn/retractEntity repo-id]]))))

(defn source-files
  "Given a user and project name, return a sorted list of the analyzed
  files in the repository. For now, this is just the Clojure files."
  [user name]
  (let [repo (get-repo user name)]
    (if (nil? repo)
      '()
      (sort (map first (q '[:find ?filepath
                            :in $ ?repo
                            :where
                            [?repo :repo/files ?files]
                            [?files :file/path ?filepath]]
                          (db @conn) repo))))))

(defn get-filepath
  "Given a user, project, and filename, return the filepath associated
  with that file."
  [user project filename]
  (let [repo (get-repo user project)]
    (ffirst (q '[:find ?filepath
                 :in $ ?repo ?filename
                 :where
                 [?repo :repo/files ?file]
                 [?file :file/name ?filename]
                 [?file :file/path ?filepath]]
               (db @conn) repo filename))))

(def note->key
  {:kibit     :note.source/kibit
   :eastwood  :note.source/eastwood
   :user      :note.source/user
   :bikeshed  :note.source/bikeshed
   :cloverage :note.source/cloverage})

(defn add-note
  "Insert a note in the database."
  [filepath line-number content type]
  (let [file (ffirst (q '[:find ?file
                          :in $ ?filepath
                          :where [?file :file/path ?filepath]]
                        (db @conn) filepath))
        note-key (type note->key)
        note-id (d/tempid :db.part/user)
        note-hash (hash (str filepath line-number type))]
    (if (nil? file)
      (throw (IllegalArgumentException.
              (str "Filepath " filepath " is not in the database.")))
      @(d/transact @conn [{:db/id note-id
                           :note/source note-key
                           :note/line-number line-number
                           :note/content content
                           :note/hash note-hash}
                          {:db/id file
                           :file/notes note-id}]))))

(defn get-notes
  "Given either a full filepath, or a user, project, and filename,
  return the notes associated with that file. The notes are database
  entities with keys that correspond to attributes defined in the
  database schema. See 'resources/data/schema.edn' for details."
  ([user project filename]
     (get-notes (get-filepath user project filename)))
  ([user project]
     (map (partial get-notes) (source-files user project)))
  ([filepath]
     (map #(d/touch (d/entity (db @conn) (first %)))
          (q '[:find ?notes
               :in $ ?filepath
               :where
               [?file :file/path ?filepath]
               [?file :file/notes ?notes]]
             (db @conn) filepath))))

(defn remove-note
  "Retract a note from the database."
  [filepath line-number content type]
  (let [file (ffirst (q '[:find ?file
                          :in $ ?filepath
                          :where [?file :file/path ?filepath]]
                        (db @conn) filepath))
        note-key (type note->key)
        note (ffirst (q '[:find ?note
                          :in $ ?file ?line-number ?content ?type
                          :where
                          [?note :note/file ?file]
                          [?note :note/source ?type]
                          [?note :note/line-number ?line-number]
                          [?note :note/content ?content]]
                        (db @conn) file line-number content note-key))]
    (when-not (nil? file)
      @(d/transact @conn [[:db.fn/retractEntity note]]))))
