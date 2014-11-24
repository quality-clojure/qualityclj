(ns qualityclj.linters.docker
  (:require [clojure.java.shell :refer [sh]]
            [qualityclj.models.db :as db])
  (:import java.io.File))

(defn run
  "Given a user/org name, a project name, and a command to run, run
  the command in the target project directory and return the output.
  
  cmd should be a collection of strings e.g. [\"lein\" \"eastwood\"]

  Note: this will fail if the database is not storing an absolute path
  for repo-path - docker requires an absolute path."
  [user project cmd]
  (let [cmd (concat ["docker" "run" "-w" "/app" "-v"
                     (str (db/repo-path) File/separator
                          user File/separator project ":/app")
                     "qualityclj/eastwood"]
                    cmd)]
    (println "Docker command: " cmd)
    (apply sh cmd)))
