(ns qualityclj.util_test
  (:require [qualityclj.util :as util])
  (:import (java.io File)
           (java.lang String))
  (:use clojure.test))

(deftest correct-path-test
  (testing "Correct file path separation"
  (if (= File/separator "/")
    (is (.contains (util/correct-path "test/qualityclj") "/"))
    (is (.contains (util/correct-path "test\\qualityclj") "\\")))))

