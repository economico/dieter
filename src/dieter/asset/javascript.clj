(ns dieter.asset.javascript
  (:use
   [dieter.asset :only [read-asset wrap-content]]
   [dieter.util :only [slurp-into string-builder]])
  (:import
   [com.google.javascript.jscomp JSSourceFile CompilerOptions CompilationLevel WarningLevel]
   [java.util.logging Logger Level]))

(defn make-compiler [log-level]
  (let [compiler (com.google.javascript.jscomp.Compiler.)
        options (CompilerOptions.)]
    (.setOptionsForCompilationLevel (CompilationLevel/SIMPLE_OPTIMIZATIONS) options)
    (if (= :quiet log-level)
      (do
        (.setOptionsForWarningLevel (WarningLevel/QUIET) options)
        (.setLevel (Logger/getLogger "com.google.javascript.jscomp") Level/OFF))
      (do
        (.setOptionsForWarningLevel (WarningLevel/VERBOSE) options)
        (.setLevel (Logger/getLogger "com.google.javascript.jscomp") Level/WARNING)))
    [compiler options]))

(defn compress-js [filename text options]
  (let [[compiler options] (make-compiler (:log-level options))]
    (.compile compiler
              (make-array JSSourceFile 0)
              (into-array JSSourceFile [(JSSourceFile/fromCode (str filename) (str text))])
              options)
    (let [source (.toSource compiler)]
      (if (.isEmpty source)
        text
        source))))

(defrecord Js [file content last-modified composed-of]
  dieter.asset.Asset
  (read-asset [this options]
    (if (= 1 (count (:composed-of this)))
      (read-asset (first (:composed-of this)) options)
      (assoc this
        :content (slurp-into
                  (wrap-content (:file this) "")
                  (:file this))
        :last-modified (.lastModified (:file this)))))

  dieter.asset.Compressor
  (compress [this options]
    (if (:compress options)
      (compress-js (:file this) (:content this) options)
      (:content this))))
