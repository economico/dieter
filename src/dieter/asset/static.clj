(ns dieter.asset.static
  (:require dieter.asset))

(defrecord Static [file content last-modified]
  dieter.asset.Asset
  (read-asset [this options]
    (assoc this
      :content (with-open [in (java.io.BufferedInputStream. (java.io.FileInputStream. (:file this)))]
                 (let [buf (make-array Byte/TYPE (.length (:file this)))]
                   (.read in buf)
                   buf))
      :last-modified (.lastModified (:file this))))

  dieter.asset.Compressor
  (compress [this options]
    (:content this)))
