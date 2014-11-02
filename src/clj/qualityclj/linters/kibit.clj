(ns qualityclj.linters.kibit
  (:require [qualityclj.models.db :as db]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as s]
            [taoensso.timbre :as timbre :refer [info]])
  (:import java.io.File))

(defn parse-comment
  "Parse a single recommendation."
  [reco repo-path]
  (let [path (s/replace-first (first reco)
                              (re-pattern (str ".*" repo-path "/")) "")
        line-number (read-string (second reco))
        content (s/trim (s/join ":" (subvec reco 2)))]
    {:filepath path :line-number line-number :content content}))

(defn parse-kibit-output
  "Return a vector of maps representing kibit recommendations.

  Each map has the following keys:
  filepath: this is relative to the
  repo-path, and what is expected for entry to the database
  line-number: line associated with the recommmendation
  content: the actual recommendation from kibit"
  [output repo-path]
  (let [comments (rest (s/split output #"At "))
        splits (mapv #(s/split %1 #":") comments)
        entries (mapv #(parse-comment %1 repo-path) splits)]
    (doseq [entry entries]
      (db/add-note (:filepath entry)
                   (:line-number entry)
                   (:content entry)
                   :kibit))))

(defn kibitize-project
  "Run kibit over the provided project and
  use the reporter for kibit's output."
  [user project repo-path]
  ;; This could be faster (the JVM startup is several seconds), with a
  ;; 'which' for *nixes or something like 'gcm' for windows, but this
  ;; check should be universal.
  (if (= 0 (:exit (sh "lein")))
    (parse-kibit-output (:out (sh "lein" "kibit"
                                  :dir (s/join File/separator
                                               [repo-path user project])))
                        repo-path)))
