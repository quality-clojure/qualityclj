(ns qualityclj.models.db
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [datomic.api :as d :refer [q db]]
            [environ.core :refer [env]])
  (:import java.io.File))

(def schema-tx (read-string (slurp (io/resource "data/schema.edn"))))
(def fixtures (read-string (slurp (io/resource "data/initial.edn"))))

(defonce conn (atom nil))

(defn ensure-db
  ([] (ensure-db (:db-uri env)))
  ([uri]
     (let [new? (d/create-database uri)]
       (reset! conn (d/connect uri))
       (when new?
         @(d/transact @conn schema-tx)
         @(d/transact @conn fixtures)))))

(defn file->entity
  "Given a File, convert it to the appropriate db entity ready to be
  added."
  [file repo-path]
  (let [file-id (d/tempid :db.part/user)]
    {:db/id file-id
     :file/name (.getName file)
     :file/path (s/replace-first (.getPath file)
                                 (str repo-path File/separator) "")}))

(defn import-repo
  "Import a git repository into the database."
  [uri user name files repo-path]
  (let [repo-id (d/tempid :db.part/user)
        file-adds (mapv #(file->entity % repo-path) files)]
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

(defn valid-filepath
  "Checks whether the filepath is known in the database. Returns a
  boolean."
  [filepath]
  (not (empty? (q '[:find ?file
                    :in $ ?filepath
                    :where [?file :file/path ?filepath]]
                  (db @conn) filepath))))

(def note->key
  {:kibit     :note.source/kibit
   :eastwood  :note.source/eastwood
   :user      :note.source/user
   :bikeshed  :note.source/bikeshed
   :cloverage :note.source/cloverage})

(defn hash-note
  "Return what should a db-unique hash for a note.

  NOTE: the note 'type' is the key in the 'note->key' map."
  [filepath line-number type]
  (hash (str filepath line-number type)))

(defn add-note
  "Insert a note in the database."
  [filepath line-number content type]
  (let [file (ffirst (q '[:find ?file
                          :in $ ?filepath
                          :where [?file :file/path ?filepath]]
                        (db @conn) filepath))
        note-key (type note->key)
        note-id (d/tempid :db.part/user)
        note-hash (hash-note filepath line-number type)]
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

(defn get-all-notes
  "Get all notes currently in the database."
  []
  (mapv #(d/touch (d/entity (db @conn) (first %)))
        (q '[:find ?note :where [?note :note/source]] (db @conn))))

(defn get-notes
  "Given a filepath, return the notes associated with that file.

  Given a user and project, return all notes associated with the project.

  The notes are database entities with keys that correspond to
  attributes defined in the database schema. See
  'resources/data/schema.edn' for details."
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
  [filepath line-number type]
  (let [note-hash (hash-note filepath line-number type)
        note (ffirst (q '[:find ?note
                          :in $ ?hash
                          :where
                          [?note :note/hash ?hash]]
                        (db @conn) note-hash))]
    (when-not (nil? note)
      @(d/transact @conn [[:db.fn/retractEntity note]]))))
