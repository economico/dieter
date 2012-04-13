(ns dieter.asset.coffeescript
  (:use
   [dieter.asset :only [wrap-content]]
   [dieter.rhino :only [call with-scope make-pool]]
   [dieter.util :only [string-builder]]
   [dieter.asset.javascript :only [map->Js]]))

(def pool (make-pool))

(defn compile-coffeescript [input filename]
  (with-scope pool ["coffee-script.js" "coffee-wrapper.js"]
    (call "compileCoffeeScript" input filename)))

(defn preprocess-coffeescript [file]
  (compile-coffeescript (slurp file) (.getCanonicalPath file)))

(defrecord Coffee [file last-modified]
  dieter.asset.Asset
  (read-asset [this options]
    (let [file (:file this)
          modified (.lastModified file)]
      (map->Js {:file file
                :content (wrap-content file (preprocess-coffeescript file))
                :last-modified modified
                :composed-of [(assoc this :last-modified modified)]}))))
