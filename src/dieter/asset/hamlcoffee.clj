(ns dieter.asset.hamlcoffee
  (:use dieter.rhino)
  (:use [dieter.asset.javascript :only [map->Js]])
  (:require dieter.asset)
  (:require [clojure.string :as cstr]))

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

(defrecord HamlCoffee [file]
  dieter.asset.Asset
  (read-asset [this options]
    (map->Js {:file (:file this)
              :content (preprocess-hamlcoffee (:file this))
              :last-modified (.lastModified (:file this))
              :composed-of [this]})))
