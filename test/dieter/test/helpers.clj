(ns dieter.test.helpers
  (:use dieter.asset)
  (:use clojure.test))

(defn has-text?
  "returns true if expected occurs in text exactly n times (one or more times if not specified)"
  ([text expected]
     (not= -1 (.indexOf text expected)))
  ([text expected times]
     (= times (count (re-seq (re-pattern expected) text)))))

(defn asset-compiles-to [asset target-type]
  (let [after-read (read-asset asset {})]
    (is (= target-type (class after-read)))
    (is (not= nil (:last-modified after-read)))
    (is (not= nil (:content after-read)))
    (is (= (:file asset) (:file (first (:composed-of after-read)))))
    (is (not= nil (:last-modified (first (:composed-of after-read)))))))
