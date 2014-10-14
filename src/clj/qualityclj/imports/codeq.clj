(ns qualityclj.imports.codeq
  (:require [qualityclj.models.db :refer [get-repo uri]]
            [taoensso.timbre :as timbre]
            [datomic.codeq.core :refer [codeq]])
  (:import java.io.File))

(timbre/refer-timbre)

(def repo-path "repos")

(defn import-repo
  "Feed the codeq from the local repository directory."
  [user project]
  (let [repo-filepath (str repo-path
                           File/separator user
                           File/separator project)]
    (when (nil? (get-repo user project))
      (debug "Importing repo " user "/" project " into codeq.")
      (codeq uri repo-filepath))))
