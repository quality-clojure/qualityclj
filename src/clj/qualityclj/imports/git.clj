(ns qualityclj.imports.git
  (:require [clj-jgit.porcelain :as git]
            [clojure.java.io :as io])
  (:import java.io.File))

(defn import-repo
  "Given a correctly formed url for a git repo, the function will
  clone and pull the repo to the specified local directory. Local
  directory must be empty, or else.

  Example: (import-repo \"https://github.com/Datomic/codeq\"
  \"Datomic\" \"codeq\")"
  [repo-url user repo repo-path]
  (git/git-clone-full repo-url (str repo-path
                                    File/separator user
                                    File/separator repo)))

(defn remove-repo
  "Given a user/org name and a project name, remove the repo from the
  filesystem."
  [user project repo-path]
  (let [dir (io/file (str repo-path
                          File/separator user
                          File/separator project))]
    (mapv io/delete-file (reverse (file-seq dir)))))
