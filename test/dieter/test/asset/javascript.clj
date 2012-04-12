(ns dieter.test.asset.javascript
  (:use dieter.asset.javascript)
  (:import dieter.asset.javascript.Js)
  (:use dieter.asset)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-read-asset-js
  (let [asset (read-asset (map->Js { :file (io/file "test/fixtures/assets/javascripts/app.js")}) {})]
    (testing "adds a source comment"
      (is (has-text? (:content asset) "/* Source: test/fixtures/assets/javascripts/app.js */")))
    (testing "includes file contents"
      (is (has-text? (:content asset) "var file = \"/app.js\"")))
    (testing "includes last-modified date"
      (is (not= nil (:last-modified asset))))))

(deftest test-compress-js
  (testing "valid javascript"
    (let [uncompressed-js " var foo = 'bar'; "
          asset (map->Js {:file "filename.js" :content uncompressed-js})]
      (testing "compression disabled"
        (is (= uncompressed-js
               (compress asset {:compress false}))))

      (testing "compression enabled"
        (is (= "var foo=\"bar\";"
               (compress asset {:compress true}))))))

  (testing "with compile errors"
    (let [uncompressed-with-errors "var foo = [1, 2, 3, ];"
          asset (map->Js {:file "haz-errors.js" :content uncompressed-with-errors})]
      (is (= uncompressed-with-errors
             (compress asset {:compress true :log-level :quiet}))))))
