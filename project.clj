(defproject psfclj "0.1.0-SNAPSHOT"
  :description "PSF Parser"
  :url "http://electronics-and-drives.de"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.trace "0.7.9"]
                 [instaparse "1.4.10"]]
  :main ^:skip-aot psfclj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
