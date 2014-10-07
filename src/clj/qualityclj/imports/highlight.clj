(ns qualityclj.imports.highlight
  (:require [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [clojure.string :as s]))

(def repo-path "repos")
(def highlight-path "highlight")

(defn highlight
  "Produce an HTML file from a given source file. Takes a full path to
  the original source file as well as an output file path."
  [filename outname]
  (let [result (sh "pygmentize" "-f" "html" "-o" outname filename)]
    (when-not (= 0 (:exit result))
      (throw (Exception. (str "Error with highlighting: " (:err result)))))))

(defn highlight-project
  "Given a project name of the form 'user/repo', highlight everything
  in the project.

  Note: in it's current form, this assumes that everything that's
  considered source code in need of highlighting is located under the
  'src' folder at the root of the repository."
  [project]
  (let [src-path (io/file (str repo-path "/" project "/src"))
        out-path (io/file (str highlight-path "/" project "/"))]
    (when-not (.exists out-path) (.mkdirs out-path))
    (doseq [file (file-seq src-path)]
      (when (and (.isFile file) (.endsWith (.getPath file) "clj"))
        (let [out-file (io/file (str (s/replace-first
                                      (.getPath file)
                                      repo-path
                                      highlight-path)
                                     ".html"))]
          (.mkdirs (.getParentFile out-file))
          (highlight (.getPath file) (.getPath out-file)))))))
