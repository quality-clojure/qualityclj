(ns qualityclj.import
  (:require [qualityclj.imports.db :as db]
            [qualityclj.imports.git :as git]
            [qualityclj.imports.highlight :as highlight]
            [qualityclj.linters.kibit :as kibit]
            [clojure.core.async :refer [<! >!  go  go-loop chan]]
            [clojure.string :as s]))

(defonce in-chan (chan))
(defonce out-chan (chan))

(def repo-path "repos")
(def highlight-path "highlight")
(def src-path "src")
(def test-path "test")

(defn extract-user-project
  "Given a git URL, extract the user/org name and the project name."
  [url]
  (let [splits (s/split url #"[:/]")
        project (last splits)
        project (s/replace project #".git$" "")
        user (last (butlast splits))]
    (if-not (or (nil? user) (nil? project))
      [user project]
      (throw (IllegalArgumentException. "Not a valid git URL.")))))

(defn import-or-update-project
  "Given a valid git URL, run the gamut of import functions. Will
  either import a new project, or update an existing one. On update,
  will pull any new commits, and update highlighting and linters."
  [url]
  (go (>! in-chan {:id :import :url url})))

(defn remove-project
  "Given a user/org name and a project name, remove all traces of the
  project."
  [user project]
  (git/remove-repo user project repo-path)
  (highlight/remove-project user project highlight-path)
  (db/remove-project user project))

(defn send-status
  "Send current import status to the out channel."
  [msg type]
  (go (>! out-chan {:id :import/status :message msg :type type})))

(defmulti import-msg-handler "Dispatch import actions" :id)

(defmethod import-msg-handler :import
  [{:keys [url]}]
  (go
    (send-status "Starting import" :info)
    (try
      (let [[user project] (extract-user-project url)]
        (git/import-repo url user project repo-path)
        (send-status "Git import complete, now highlighting." :info)
        (highlight/highlight-project user project src-path test-path
                                     repo-path highlight-path)
        (send-status "Highlighting complete, now adding to database." :info)
        (db/import-project url user project repo-path)
        (send-status "Project imported to database, now linting." :info)
        (kibit/kibitize-project user project repo-path))
      (>! out-chan {:id :import/status
                    :message "Import complete!"
                    :type :success})
      (catch Exception e (>! out-chan {:id :import/status
                                       :message (.getMessage e)
                                       :type :danger})))))

(go-loop [msg (<! in-chan)]
  (import-msg-handler msg)
  (recur (<! in-chan)))
