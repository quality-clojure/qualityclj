(ns qualityclj.imports.highlight
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as s])
  (:import java.io.File))

(defn highlight
  "Produce an HTML file from a given source file. Takes a full path to
  the original source file as well as an output file path."
  [filename outname]
  (let [result (sh "pygmentize" "-f" "html" "-o" outname filename)]
    (when-not (= 0 (:exit result))
      (throw (Exception. (str "Error with highlighting: " (:err result)))))))

(defn highlight-project
  "Given a username and a project name, highlight everything in the
  project.

  Note: in it's current form, this assumes that everything that's
  considered source code in need of highlighting is located under the
  'src' folder at the root of the repository.

  TODO: Issue #20. Parse the source directory defined in project.clj
  and use that for highlighting.

  TODO: Issue #21. This currently only targets Clojure src files, not
  ClojureScript, or test files."
  [user project src-path repo-path highlight-path]
  (let [result (sh "pygmentize")]
    (when-not (= 0 (:exit result))
      (throw (Exception. "Pygments is not available on the execution path."))))
  (let [src-path (io/file (s/join File/separator
                                  [repo-path user project src-path]))]
    (if-not (.exists src-path)
      (throw (IllegalArgumentException. "Project does not exist!"))
      (doseq [file (file-seq src-path)]
        (when (and (.isFile file) (.endsWith (.getPath file) "clj"))
          (let [out-file (io/file (str (s/replace-first
                                        (.getPath file)
                                        repo-path
                                        highlight-path)
                                       ".html"))]
            (.mkdirs (.getParentFile out-file))
            (highlight (.getPath file) (.getPath out-file))))))))

(defn remove-project
  "Given a user/org name, a project name, and the highlight folder
  prefix, remove a project's highlighted source files."
  [user project highlight-path]
  (let [dir (io/file (str highlight-path
                          File/separator user
                          File/separator project))]
    (mapv io/delete-file (reverse (file-seq dir)))))
