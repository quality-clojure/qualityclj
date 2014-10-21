(ns qualityclj.core_test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [qualityclj.core :as core]
            [cemerick.cljs.test :as t]))

(deftest simple-test
  (is (= 1 1)))
