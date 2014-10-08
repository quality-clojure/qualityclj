(ns qualityclj.imports.git
  (:require [clj-jgit.porcelain :as git]))

(def repo-path "repos")

(defn import-repo 
  "Given a correctly formed url for a git repo, the function will clone and pull
   the repo to the specified local directory. Local directory must be empty, or 
   else. 

   Example: (import-repo \"https://github.com/Datomic/codeq\" \"Datomic\" \"codeq\")"
  [repo-url user repo]
  (git/git-clone-full repo-url (str repo-path "/" user "/" repo)))
