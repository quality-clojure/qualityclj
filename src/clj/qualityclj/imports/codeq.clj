(ns qualityclj.imports.codeq
  (:require [qualityclj.models.db :refer [get-repo]])
  (:import java.io.File))


(def uri "datomic:free://localhost:4334/qualityclj")
(def repo-path "repos")
(def codeq-path "../../../codeq-0.1.0-SNAPSHOT-standalone.jar")

(defn import-repo 
  "Feed the codeq from the local repository directory."
  [user repo]
  (let [codeq-filepath (.getPath (clojure.java.io/file codeq-path))
        repo-filepath (.getAbsolutePath (clojure.java.io/file 
                                          (str repo-path 
                                            File/separator user 
                                            File/separator repo)))]
    (when (nil? (get-repo user repo))
      (clojure.java.shell/sh "java" "-server" "-Xmx1g" "-jar" 
        codeq-filepath uri :dir repo-filepath))))