(ns qualityclj.imports.db
  (:require [qualityclj.models.db :as db]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:import java.io.File))

(defn import-project
  "Given a username and a project name, import it into the database.

  Note: in it's current form, this assumes that everything that's
  considered source code in need of importing is located under the
  'src' folder at the root of the repository.

  TODO: Issue #20. Parse the source directory defined in project.clj
  and use that for importing.

  TODO: Issue #21. This currently only targets Clojure and Clojurescript
  src files, not test files."
  [uri user project src-path test-path repo-path]
  (let [src-path-file (io/file (s/join File/separator
                                       [repo-path user project src-path]))
        test-path-file (io/file (s/join File/separator
                                        [repo-path user project test-path]))
        files (filter #(and (.isFile %)
                            (or (.endsWith (.getPath %) "clj")
                                (.endsWith (.getPath %) "cljs")))
                      (concat (file-seq src-path-file)
                              (file-seq test-path-file)))]
    (db/import-repo uri user project files repo-path)))

(defn remove-project
  "Given a user/org name and a project name, remove relevant entries
  from the database."
  [user project]
  (db/remove-repo user project))
