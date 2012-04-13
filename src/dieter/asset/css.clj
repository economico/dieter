(ns dieter.asset.css
  (:use
   [dieter.asset :only [read-asset wrap-content]]
   [dieter.util :only [slurp-into string-builder]])
  (:require
   [clojure.string :as s]))

(defn compress-css [text]
  (-> text
      (s/replace "\n" "")
      (s/replace #"\s+" " ")
      (s/replace #"^\s" "")))

(defrecord Css [file content last-modified composed-of]
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
      (compress-css (:content this))
      (:content this))))
