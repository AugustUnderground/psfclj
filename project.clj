(defproject psfclj "0.1.0-SNAPSHOT"
  :description "PSF Parser for JVM"
  :url "http://electronics-and-drives.de"
  :license {:name "MIT License (MIT)"
            :url "https://mit-license.org"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.trace "0.7.9"]
                 [instaparse "1.4.10"]]
  :main ^:skip-aot psfclj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
