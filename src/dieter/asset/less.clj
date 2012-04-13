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

(defrecord Less [file last-modified]
  dieter.asset.Asset
  (read-asset [this options]
    (let [file (:file this)
          modified (.lastModified file)]
      (map->Css {:file file
                 :content (dieter.asset/wrap-content file (preprocess-less file))
                 :last-modified modified
                 :composed-of [(assoc this :last-modified modified)]}))))
