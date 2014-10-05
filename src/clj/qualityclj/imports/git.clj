(ns qualityclj.imports.git
  (:require [clj-jgit.porcelain :as git]))

(defn import-repo 
  "Given a correctly formed url for a git repo, the function will clone and pull
   the repo to the specified local directory. Local directory must be empty, or 
   else."
  [repo-url repo-dir]
  (git/git-clone-full repo-url (str "repos/" repo-dir)))
