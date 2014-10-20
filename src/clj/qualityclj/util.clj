(ns qualityclj.util
  (:import (java.io File))
  (:require [clojure.java.io :as io]))

(defn correct-path
  "Make sure a string representing a path has the correct path
  separators. This is only needed for compliance with Windows,
  generally."
  [path]
  (.getPath ^File (io/file path)))
