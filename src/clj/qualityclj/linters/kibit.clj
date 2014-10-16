(ns qualityclj.linters.kibit
  (:require [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [kibit.check :as check]
            [qualityclj.models.db :as db]
            [clojure.string :as s])
  (:import java.io.File))

(timbre/refer-timbre)

(def repo-path "repos")

#_(defn read-kibit-report-from-file
    "Reads in a plain-text kibit-report-to-file."
    [file]
    (with-open [rdr (io/reader (io/resource file))]
      (doseq [line (line-seq rdr)]
        (println line))))

#_(defn write-kibit-report-to-file
    "Write a check-map in plain text to specified file."
    [output-file check-map]
    (let [{:keys [file line expr alt]} check-map]
      (with-open [wrtr (io/writer (io/file output-file))]
        (.write wrtr (pr-str check-map))
        (.write wrtr (str "\n")))))

(defn note-reporter
  "Send the result from kibit to the db as a note."
  [check-map]
  (let [{:keys [file line expr alt]} check-map
        content (str "Instead of:\n\t" expr "\nTry:\n\t" alt)]
    (db/add-note file line content :kibit)))

(defn kibitize-file
  "Run kibit over the provided project and
  use the reporter for kibit's output."
  [user project reporter]
  (let [src-folder "src"
        src-path (io/file
                  (s/join File/separator [repo-path user project src-folder]))
        files (filter #(and (.isFile %) (.endsWith (.getPath %) "clj"))
                      (file-seq src-path))]
    ;; Map check-file over each file returned from the filter.
    (map #(check/check-file (.getPath %1) :reporter reporter) files)))
