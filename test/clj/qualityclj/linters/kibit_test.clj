(ns qualityclj.linters.kibit_test
  (:require [qualityclj.linters.kibit :as kibit])
  (:use clojure.test))



;; Will need to access the db when running the kibitize-file-test
;; to confirm the output.
;;(deftest kibitize-file-test
;;  (testing "function with known output."
;;    (is (= (kibit/kibitize-file code-with-kibit-output)
;;           nil))))
