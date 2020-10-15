(defproject psfclj "0.1.0-SNAPSHOT"
  :description "PSF Parser for JVM"
  :url "http://electronics-and-drives.de"
  :license {:name "MIT License (MIT)"
            :url "https://mit-license.org"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "1.0.0"]
                 [org.clojure/tools.cli "1.0.194"]
                 [instaparse "1.4.10"]]
  :target-path "target/%s"
  :aot [psfclj.core psfclj.PSFParser]
  :main psfclj.core ;^:skip-aot psfclj.core
  :profiles {:uberjar {:aot :all}})
