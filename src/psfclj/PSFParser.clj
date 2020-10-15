(ns psfclj.PSFParser
  (:require [psfclj.core :as core]
            [instaparse.core :as insta]
            [clojure.java.io :as io])
  (:gen-class :name psfclj.PSFParser.Parser
              :state state
              :init init
              :prefix "psf-"
              :main false
              :methods [[setGrammar [String] void]
                        [parsePSF [String] clojure.lang.PersistentArrayMap]]))

(defn psf-init [grammar-file-name]
  (let [psf-bnf (insta/parser (if (.exists (io/file grammar-file-name))
                                  (io/file grammar-file-name) 
                                  (io/resource "psf.bnf")))]
    [[] (atom {:grammar psf-bnf})]))

(defn psf-setGrammar [this grammar-file-name]
  (let [psf-bnf (insta/parser (io/file grammar-file-name))]
    (swap! (.state this) into {:grammar psf-bnf})))

(defn psf-parsePSF [this psf-content]
  (let [psf-bnf (@(.state this) :grammar)]
    (core/parse-psf psf-content psf-bnf)))
