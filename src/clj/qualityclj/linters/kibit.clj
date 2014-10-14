(ns qualityclj.linters.kibit)

#_(defn read-kibit-report-from-file
  "Reads in a plain-text kibit-report-to-file."
  [file]
  (with-open [rdr (io/reader (io/resource file))]
    (doseq [line (line-seq rdr)]
      (println line))))

#_(defn write-kibit-report-to-file
  "Write a check-map in plain text to specified file."
  [output-file check-map]
  (let [{:keys [file line expr alt]} check-map]
    (with-open [wrtr (io/writer (io/file output-file))]
      (.write wrtr (pr-str check-map))
      (.write wrtr (str "\n")))))

#_(defn run-kibit-over-file
  "Run kibit over the provided source file and output to the provided
  file location. For now, both must be located in resources"
  [source-file output-file]
  (check/check-file (io/resource source-file)
                    :reporter (partial write-kibit-report-to-file
                                       (io/resource output-file))))
