(ns qualityclj.import
  (:require [qualityclj.imports.db :as db]
            [qualityclj.imports.git :as git]
            [qualityclj.imports.highlight :as highlight]
            [qualityclj.linters.kibit :as kibit]
            [clojure.string :as s]))

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
  (let [[user project] (extract-user-project url)]
    (git/import-repo url user project repo-path)
    (highlight/highlight-project user project src-path test-path
                                 repo-path highlight-path)
    (db/import-project url user project repo-path)
    (kibit/kibitize-project user project kibit/note-reporter repo-path)))

(defn remove-project
  "Given a user/org name and a project name, remove all traces of the
  project."
  [user project]
  (git/remove-repo user project repo-path)
  (highlight/remove-project user project highlight-path)
  (db/remove-project user project))
