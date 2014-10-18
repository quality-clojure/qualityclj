(ns qualityclj.imports.highlight
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as s])
  (:import java.io.File))

(defn highlight
  "Produce an HTML file from a given source file. Takes a full path to
  the original source file as well as an output file path."
  [filename outname]
  (let [result (sh "pygmentize" "-f" "html" "-O" "linenos=1"
                   "-l" "clojure" "-o" outname filename)]
    (when-not (= 0 (:exit result))
      (throw (Exception. (str "Error with highlighting: " (:err result)))))))

(defn highlight-directory
  "Given a path to a directory, highlight everything in the directory
  matching an acceptable file extentsion (i.e., clj and cljs)."
  [path repo-path highlight-path]
  (doseq [file (file-seq path)]
    (when
        (and (.isFile file)
             (or (.endsWith (.getPath file) "clj")
                 (.endsWith (.getPath file) "cljs")))
      (let [out-file (io/file (str (s/replace-first
                                    (.getPath file)
                                    repo-path
                                    highlight-path)
                                   ".html"))]
        (.mkdirs (.getParentFile out-file))
        (highlight (.getPath file) (.getPath out-file))))))

(defn highlight-project
  "Given a username and a project name, highlight everything in the
  project.

  Note: in it's current form, this assumes that everything that's
  considered source code in need of highlighting is located under the
  'src' folder at the root of the repository.

  TODO: Issue #20. Parse the source directory defined in project.clj
  and use that for highlighting."
  [user project src-path test-path repo-path highlight-path]
  (let [result (sh "pygmentize")]
    (when-not (= 0 (:exit result))
      (throw (Exception. "Pygments is not available on the execution path."))))
  (let [src-path-file (io/file (s/join File/separator
                                       [repo-path user project src-path]))
        test-path-file (io/file (s/join File/separator
                                        [repo-path user project test-path]))]
    (if-not (or (.exists src-path-file) (.exists test-path-file))
      (throw (IllegalArgumentException. "Invalid highlighting path!"))
      (do
        (highlight-directory src-path-file repo-path highlight-path)
        (highlight-directory test-path-file repo-path highlight-path)))))

(defn remove-project
  "Given a user/org name, a project name, and the highlight folder
  prefix, remove a project's highlighted source files."
  [user project highlight-path]
  (let [dir (io/file (str highlight-path
                          File/separator user
                          File/separator project))]
    (mapv io/delete-file (reverse (file-seq dir)))))
