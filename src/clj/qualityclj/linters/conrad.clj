(ns qualityclj.linters.conrad
  (:require [qualityclj.models.db :as db]
            [clojure.core.async :refer [chan <!! close!]]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [taoensso.timbre :as timbre :refer [info]]
            [conrad.core :refer [check-files]])
  (:import java.io.File))

(defn- add-note
  "Add a note from conrad to the database."
  [note repo-path]
  (info "Adding note: " note)
  (let [path (s/replace-first (:filename note)
                              (re-pattern (str ".*" repo-path "/")) "")]
    (db/add-note path (:line-num note) (:message note) :conrad)))

(defn- lint-directory
  "Given a path to a directory, lint everything in the directory
  matching an acceptable file extension (i.e., clj and cljs)."
  [path ch]
  (let [files (filter #(and (.isFile %1)
                            (or (.endsWith (.getPath %1) "clj")
                                (.endsWith (.getPath %1) "cljs")))
                      (file-seq path))]
    (check-files files ch)))

(defn check-project
  "Run conrad over a project and add notes to the database."
  [user project src-path test-path repo-path]
  (let [src-path-file (io/file (s/join File/separator
                                       [repo-path user project src-path]))
        test-path-file (io/file (s/join File/separator
                                        [repo-path user project test-path]))
        ch (chan)]
    (when (.exists src-path-file) (lint-directory src-path-file ch))
    (when (.exists test-path-file) (lint-directory test-path-file ch))
    (loop [note (<!! ch)]
      (if (= :done note)
        (close! ch)
        (do
          (add-note note repo-path)
          (recur (<!! ch)))))))
