(ns dieter.asset.less
  (:use
   [dieter.rhino :only (with-scope make-pool call)]
   [dieter.asset.css :only [map->Css]])
  (:require
   dieter.asset
   dieter.asset.css))

(def pool (make-pool))

(defn preprocess-less [file]
  (with-scope pool ["less-wrapper.js" "less-rhino-1.2.1.js"]
    (call "compileLess" (.getCanonicalPath file))))

(defrecord Less [file]
  dieter.asset.Asset
  (read-asset [this options]
    (map->Css {:file (:file this)
               :content (preprocess-less (:file this))
               :last-modified (.lastModified (:file this))
               :composed-of [this]})))
