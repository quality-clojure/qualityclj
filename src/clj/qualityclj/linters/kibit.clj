(ns qualityclj.linters.kibit
  (:require [taoensso.timbre :as timbre :refer [info]]
            [clojure.java.io :as io]
            [kibit.check :as check]
            [qualityclj.models.db :as db]
            [clojure.string :as s]
            [clojure.pprint :as pp])
  (:import java.io.File
           java.io.StringWriter))

(defn pprint-code
  "Get the expr in the right format for printing."
  [expr]
  (let [writer (StringWriter.)]
    (pp/write expr
              :dispatch pp/code-dispatch
              :stream writer
              :pretty true)
    (str writer)))

(defn note-reporter
  "Send the result from kibit to the db as a note."
  [check-map]
  (let [{:keys [file line expr alt]} check-map
        content (str "Instead of:\n\t" (pprint-code expr) "\nTry:\n\t"
                     (pprint-code alt))]
    (db/add-note file line content :kibit)))

(defn kibitize-project
  "Run kibit over the provided project and
  use the reporter for kibit's output."
  [user project reporter repo-path]
  (let [src-folder "src"
        src-path (io/file
                  (s/join File/separator [repo-path user project src-folder]))]
    ;; Run check-file over each file returned from the filter.
    (doseq [file (filter #(and (.isFile %) (.endsWith (.getPath %) "clj"))
                         (file-seq src-path))]
      (info (.getPath file))
      (check/check-file (.getPath file) :reporter reporter))))
