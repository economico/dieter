(ns dieter.asset
  (:use
   [dieter.util :only [string-builder]]
   [dieter.path :only [file-ext]]))

(def cache "map of file objects to their representative assets"
  (atom {}))

(def types "mapping of file types to constructor functions"
  (atom {}))

(defprotocol Asset
  "Protocol for pre-processing assets"
  (read-asset [this options]
    "Perform all pre-processing on the object, setting :content and :last-modified.
    Must return an Asset."))

(defprotocol Compressor
  "Protocol for compressing assets"
  (compress [this options]
    "Perform any required compression / minification.
    Must return final contents of the file for output.
    Contents can be a String, StringBuilder, or byte[]"))

(defn register [file-ext constructor-fn]
  "register a new asset constructor for files with the file-ext"
  (swap! types assoc file-ext constructor-fn))

(defn make-asset
  "returns a newly constructed asset of the proper type as determined by the file extension.
  defaults to Static if extension is not registered."
  ([filename] (make-asset filename {}))
  ([filename opt]
     ((get @types (file-ext filename) (:default @types)) (merge {:file filename} opt))))

(defn file-changed?
  "Returns true if contents have changed since last read
  or if file has not yet been read."
  [asset]
  (or (nil? (:last-modified asset))
      (and (not (nil? (:composed-of asset)))
           (some file-changed? (:composed-of asset)))
      (> (.lastModified (:file asset)) (:last-modified asset))))

(defn get-asset [file options]
  "Given an asset, will either return cached contents
  or call read-asset to fetch contents into cache."
  (if (= :development (:cache-mode options))
    (if-let [asset (get @cache file)]
      (if (file-changed? asset)
        (let [refreshed (read-asset asset options)]
          (swap! cache assoc file refreshed)
          refreshed)
        asset)
      (let [asset (read-asset (make-asset file) options)]
        (swap! cache assoc file asset)
        asset))
    (read-asset (make-asset file) options)))

(defn wrap-content [file content]
  (string-builder "/* Source: " file " */\n" content))
