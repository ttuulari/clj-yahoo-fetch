(defproject portfolio-back "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha4"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [clj-time "0.8.0"]
                 [clj-http "1.0.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [clojure-csv/clojure-csv "2.0.1"]]
  :main portfolio-back.core
  :aot [portfolio-back.core]
  :profiles {:dev {:plugins [[lein-midje "3.1.1"]]}})
