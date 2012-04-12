(ns dieter.path
  (:use dieter.settings)
  (:require
   [clojure.string :as cstr]
   [clojure.java.io :as io])
  (:import
   [java.security MessageDigest]))

(derive (class (make-array Byte/TYPE 0)) ::bytes)
(derive java.lang.String ::string-like)
(derive java.lang.StringBuilder ::string-like)

(defmulti md5 class)

(defmethod md5 ::bytes [bytes]
  (let [digest (.digest (MessageDigest/getInstance "MD5") bytes)]
    (format "%032x" (BigInteger. 1 digest))))

(defmethod md5 ::string-like [string]
  (md5 (.getBytes (str string) "UTF-8")))

(defn add-md5 [path content]
  "Return a new path which includes the MD5 hash of the file's contents"
  (if-let [[match fname ext] (re-matches #"^(.+)\.(\w+)$" path)]
    (str fname "-" (md5 content) "." ext)
    (str path "-" (md5 content))))

(defmulti write-file (fn [c f] (class c)))

(defmethod write-file ::string-like [content file]
  (spit file content))

(defmethod write-file ::bytes [content file]
  (with-open [out (java.io.FileOutputStream. file)]
    (.write out content)))

(defn cached-file-path
  "Given the request path, generate the filename of where the file
will be cached. Cache is rooted at cache-root/assets/ so that
static file middleware can be rooted at cache-root"
  [requested-file content]
  (add-md5
   (cstr/replace-first requested-file "/assets/" (str "/" (cache-root) "/assets/"))
   content))

(defn search-dir
  "Return the directory to use as the root of a search for relative-file"
  [relative-path start-dir]
  (let [relative-file (io/file relative-path)
        relative-parent (.getParent relative-file)]
    (cond
     (re-matches #".*/$" relative-path) (io/file start-dir relative-path)
     relative-parent (io/file start-dir relative-parent)
     :else start-dir)))

(defn find-in-files
  "Find the first file whose name is an exact match, or whose basename
matches (ignoring file extension)"
  [filename files]
  (let [[_ basename] (re-matches #"(^.*?)(?:\.\w+)?$" filename)
        pattern (re-pattern (str "^" basename ".*$"))]
    (or (first (filter #(= filename (.getName %)) files))
        (first (filter #(re-matches pattern (.getName %)) files)))))

(defn find-file
  "Find the first file under start-dir which matches the partial path"
  [partial-path start-dir]
  (let [relative-file (io/file partial-path)
        filename (.getName relative-file)
        search-dir (search-dir partial-path start-dir)]
    (if (re-matches #"^\./.*" partial-path)
      (find-in-files filename (.listFiles search-dir))
      (find-in-files filename (file-seq search-dir)))))

(defn file-ext
  "Return file extension (everything after the last '.')"
  [file]
  (last (cstr/split (str file) #"\.")))

(defn uncachify-filename
  "Return the original file name with MD5 removed"
  [filename]
  (if-let [[match fname hash ext] (re-matches #"^(.+)-([\da-f]{32})\.(\w+)$" filename)]
    (str fname "." ext)
    filename))

(defn make-relative-to-cache
  "Remove the part of the path up to and including the cache root."
  [path]
  (cstr/replace-first path (re-pattern (str ".*" (cache-root))) ""))

(defmulti cache-busting-path
  "in production mode, append a md5 of the file contents to the file path"
  :cache-mode)

(defmethod cache-busting-path :development [settings path] path)

(defmethod cache-busting-path :production [settings path]
  (or (get @cached-paths path)
      (add-md5 path (str (java.util.Date.)))))
