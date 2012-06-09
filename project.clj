(defproject tugboat "0.1.0-SNAPSHOT"
  :description "distributed work queue"
  :url "http://github.com/ralfonso/tugboat"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :main tugboat.core
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/data.json "0.1.2"]
                 [clj-redis "0.0.12"]])
