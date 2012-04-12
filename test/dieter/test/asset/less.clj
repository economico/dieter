(ns dieter.test.asset.less
  (:use dieter.asset)
  (:use dieter.asset.less)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-preprocess-less
  (testing "basic less file"
    (is (= "#header {\n  color: #4d926f;\n}\n" (preprocess-less (io/file "test/fixtures/assets/stylesheets/basic.less")))))
  (testing "file with imports"
    (is (= "#includee {\n  color: white;\n}\n#includer {\n  color: black;\n}\n"
           (preprocess-less (io/file "test/fixtures/assets/stylesheets/includes.less")))))
  (testing "bad less syntax"
    (try
      (preprocess-less (io/file "test/fixtures/assets/stylesheets/bad.less"))
      (is false) ; test it throws
      (catch Exception e
        (is (has-text? (.toString e) "Syntax Error on line 1"))
        (is (has-text? (.toString e) "@import \"includeme.less\""))))))

(deftest test-less-record
  (testing "read-asset returns a Css asset"
    (let [asset (map->Less {:file (io/file "test/fixtures/assets/stylesheets/basic.less")})
          after-read (read-asset asset {})]
      (is (= dieter.asset.css.Css (class after-read)))
      (is (= asset (first (:composed-of after-read))))
      (is (not= nil (:last-modified after-read)))
      (is (not= nil (:content after-read))))))
