(ns qualityclj.import-test
  (:require [qualityclj.import :as import])
  (:use clojure.test))

(deftest extract-user-project-test
  (testing "Extracting user project name from proper url"
    (is (= ["quality-clojure" "qualityclj"] (import/extract-user-project 
      "https://github.com/quality-clojure/qualityclj.git"))))
  ;; We should also test for malformed urls...
  )

