(ns dieter.test.asset.coffeescript
  (:use dieter.asset.coffeescript)
  (:use dieter.asset)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-preprocess-coffeescript
  (testing "basic coffee file"
    (is (= "(function() {\n  (function(param) {\n    return alert(\"x\");\n  });\n}).call(this);\n"
           (preprocess-coffeescript
            (io/file "test/fixtures/assets/javascripts/test.js.coffee")))))
  (testing "syntax error"
    (try
      (preprocess-coffeescript
       (io/file "test/fixtures/assets/javascripts/bad.js.coffee"))
      (is false) ; must throw
      (catch Exception e
        (is (has-text? (.toString e) "on line 2"))
        (is (has-text? (.toString e) "bad.js.coffee"))
        (is (has-text? (.toString e) "too many ]"))))))

(deftest test-coffee-record
  (testing "read-asset returns a Js asset"
    (let [asset (map->Coffee {:file (io/file "test/fixtures/assets/javascripts/test.js.coffee")})
          after-read (read-asset asset {})]
      (is (= dieter.asset.javascript.Js (class after-read)))
      (is (= asset (first (:composed-of after-read))))
      (is (not= nil (:last-modified after-read)))
      (is (not= nil (:content after-read))))))
