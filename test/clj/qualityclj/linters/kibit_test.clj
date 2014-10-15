(ns qualityclj.linters.kibit_test
  (:require [qualityclj.linters.kibit :as kibit])
  (:use clojure.test))

(def expected-line
  "")

(def expected-expr
  "")

(def expected-alt
  "")

(deftest needs-tests
  (is (= 1 0)))

;;(defn test-reporter
;;  [check-map]
;;  (let [{:keys [line expr alt]} check-map]
;;    (is (= line expected-line))
;;    (is (= expr expected-expr))
;;    (is (= alt expected-alt))))

;; Awaiting issue #36 to finish test.
;;(deftest kibitize-file-test
;;  (testing "function with known output."
;;    (is (= (kibit/kibitize-file code-with-kibit-output (partial test-reporter))
;;           nil))))
