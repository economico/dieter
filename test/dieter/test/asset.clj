(ns dieter.test.asset
  (:use clojure.test)
  (:use dieter.asset)
  (:use [dieter.asset.static :only [map->Static]])
  (:require [clojure.java.io :as io]))

;; an example of how to define an asset
;; note that the behavior of read-asset is important
(defrecord AssetTest [file content last-modified]
    dieter.asset.Asset
    (read-asset [this options]
      ;; read asset should set content and last-modified
      ;; or return an asset of a different type with those fields set
      (assoc this
        :content "hello"
        :last-modified (.lastModified (:file this)))))

(use-fixtures :once (fn [tests]
                      (let [old-types @types
                            old-cache @cache]
                        (reset! types {})
                        (reset! cache {})
                        (tests)
                        (reset! types old-types)
                        (reset! cache old-cache))))

(deftest test-register-and-make-asset
  (register "js" map->AssetTest)
  (register :default map->Static)
  (testing "filename matches an asset"
    (is (= AssetTest (class (make-asset (io/file "test/fixtures/assets/javascripts/app.js"))))))
  (testing "filename does not match asset"
    (is (= dieter.asset.static.Static
           (class (make-asset (io/file "test/fixtures/assets/javascripts/test.js.coffee")))))))

(deftest test-get-asset
  (register "js" map->AssetTest)
  (let [file (io/file "test/fixtures/assets/javascripts/lib.js")
        orig-asset (get-asset file {})
        orig-ts (:last-modified orig-asset)]

    (testing "brand new asset"
      (is (= "hello" (:content orig-asset)))
      (is (> orig-ts 0)))

    (testing "up to date asset"
      (let [asset (get-asset file {})]
        (is (= orig-asset asset))
        (is (= orig-ts (:last-modified asset)))))

    (testing "out of date asset"
      (.setLastModified file (+ 1000 orig-ts))
      (let [asset (get-asset file {})]
        (is (not= orig-asset asset))
        (is (not= orig-ts (:last-modified asset)))
        (is (= "hello" (:content asset)))))))
