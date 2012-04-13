(ns dieter.asset.hamlcoffee
  (:use
   dieter.rhino
   [dieter.asset.javascript :only [map->Js]]
   [dieter.asset :only [wrap-content]])
  (:require
   [clojure.string :as cstr]))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(def pool (make-pool))

(defn preprocess-hamlcoffee [file]
  (with-scope pool ["coffee-script.js"
                    "haml-coffee.js"
                    "haml-coffee-assets.js"
                    "haml-coffee-wrapper.js"]
    (let [input (slurp file)
          filename (filename-without-ext file)]
      (call "compileHamlCoffee" input filename))))

(defrecord HamlCoffee [file last-modified]
  dieter.asset.Asset
  (read-asset [this options]
    (let [file (:file this)
          modified (.lastModified file)]
      (map->Js {:file file
                :content (wrap-content file (preprocess-hamlcoffee file))
                :last-modified modified
                :composed-of [(assoc this :last-modified modified)]}))))
