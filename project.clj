(defproject tugboat "0.0.5"
  :description "distributed work queue"
  :url "http://github.com/ralfonso/tugboat"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :main tugboat.core
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [log4j/log4j "1.2.16"]
                 [org.clojure/data.json "0.1.2"]
                 [clj-redis "0.0.12"]
                 [clj-time "0.4.3"]
                 [overtone/at-at "1.0.0"]
                 [com.eaio.uuid/uuid "3.2"]])
