(ns qualityclj.util
  (:require [clojure.string :as string])
  (:import java.io.File))

(defn correct-path-separators
  "Make sure a string representing a path has the correct path
  separators. This is only needed for compliance with Windows,
  generally."
  [path]
  (string/replace path #"[/\\]" File/separator))
