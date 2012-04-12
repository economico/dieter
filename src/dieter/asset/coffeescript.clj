(ns dieter.asset.coffeescript
  (:require dieter.asset)
  (:use
   [dieter.rhino :only (call with-scope make-pool)]
   [dieter.asset.javascript :only [map->Js]]))

(def pool (make-pool))

(defn compile-coffeescript [input filename]
  (with-scope pool ["coffee-script.js" "coffee-wrapper.js"]
    (str (call "compileCoffeeScript" input filename))))

(defn preprocess-coffeescript [file]
  (compile-coffeescript (slurp file) (.getCanonicalPath file)))

(defrecord Coffee [file]
  dieter.asset.Asset
  (read-asset [this options]
    (map->Js {:file (:file this)
              :content (preprocess-coffeescript (:file this))
              :last-modified (.lastModified (:file this))
              :composed-of [this]})))
