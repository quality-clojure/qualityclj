(ns qualityclj.imports.codeq
  (:require [qualityclj.models.db :refer [get-repo uri]]
            [taoensso.timbre :as timbre]
            [datomic.codeq.core :refer [codeq]])
  (:import java.io.File))

(timbre/refer-timbre)

(def repo-path "repos")

(defn import-repo
  "Feed the codeq from the local repository directory."
  [user repo]
  (let [repo-filepath (str repo-path
                           File/separator user
                           File/separator repo)]
    (when (nil? (get-repo user repo))
      (debug "Importing repo " user "/" repo " into codeq.")
      (codeq uri repo-filepath))))
