(ns qualityclj.linters.eastwood
  (:require [qualityclj.linters.docker :as docker]
            [qualityclj.models.db :as db]
            [clojure.string :as string])
  (:import (java.io BufferedReader StringReader File)))

(defn parse-comment
  "Parse a single comment."
  [comment]
  (let [parts (string/split comment #":")
        filepath (first parts)
        line-number  (read-string (second parts))
        content (string/join ":" (subvec parts 3))]
    {:filepath filepath :line-number line-number :content content}))

(defn parse-eastwood-output
  "Parse the complete output from running eastwood."
  [output user project]
  (let [lines (line-seq (BufferedReader. (StringReader. output)))
        _ (println "Lines to parse: " (count lines))
        comments (filter #(not (nil? (re-find #".*:.*:.*:.*" %))) lines)
        _ (println "Comments from eastwood: " (count comments))
        comments (map parse-comment comments)]
    (doseq [entry comments]
      (println "Adding note: " entry)
      (db/add-note (str user
                        File/separator project
                        File/separator (:filepath entry))
                   (:line-number entry)
                   (:content entry)
                   :eastwood))))

(defn eastwood-project
  "Run eastwood over the provided project in a docker container."
  [user project]
  (let [output (docker/run user project
                 ["lein" "eastwood"
                  ;;"\"{:namespaces [:source-paths]}\""
                  ])]
    (println "Output from eastwood: " output)
    (parse-eastwood-output (:out output)  user project)))
