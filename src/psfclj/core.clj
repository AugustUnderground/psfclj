(ns psfclj.core
    (:require [instaparse.core :as insta]
              [clojure.tools.trace :as tr]
              [clojure.java.io :as io])
    (:gen-class))

(def help (str "Usage: psfclj [options] <file>\n"
               "Read a PSF Data from file.\n\n"
               "Options:\n"
               "\t-g <file>\t\t Specify Grammer file."
               "\t-j\t\tJSON\n"
               "\t-x\t\tXML (NOT IMPLEMENTED!)\n"
               "\t-c\t\tCSV (values only)\n"
               "<file \tPath to valid PSF\n"
               "The output will be written to stdout and "
               "can be redirected into a file.\n\n"))

(def psf-bnf (insta/parser (clojure.java.io/resource "../resources/psf.bnf")))

(defn transpose [ll]
  (apply map list ll))

(defn depth [hm]
  (if (map? hm) 
      (inc (apply max (map depth (vals hm)))) 
      0))

(defn psf-section [psf-file psf-section]
  (first (filter #(= (second %) psf-section) psf-file)))

(defn ^:dynamic map-field [field]
  (cond (= (first field) :section)
          {(second field) (into {} (map map-field (drop 2 field)))}
        (= (first field) :attribute)
          {(second field) 
           (if (> (count field) 3)
               (into {} (map map-field (drop 2 field)))
               (if (<= (count (last field)) 2)
                   (last (last field))
                   (rest (last field))))}
        (or (= (first field) :prop)
            (= (first field) :struct))
          (into {} (map map-field (rest field)))
        (or (= (first field) :values)
            (= (first field) :types)
            (= (first field) :unit))
          {(name (first field)) 
           (if (> (count field) 2)
            (rest field) (first (rest field)))}
        :else
          {}))

(defn ^:dynamic map-value [values parameters]
  (into {}
  (map (fn [param]
         (let [param-value (map #(first (drop 2 %))
                                (filter #(= (second %) (first param)) 
                                        values))]
            {param 
             (if (map? (second param))
              (into {} (map #(hash-map %1 (map read-string %2))
                            (keys (second param))
                            (transpose (map rest param-value))))
              (map #(read-string (last %)) param-value))}))
       parameters)))

(defn map-psf [file-name]
  (let [psf     (psf-bnf (slurp file-name))
        HEADER  (map-field (psf-section psf "HEADER"))
        TYPE    (map-field (psf-section psf "TYPE"))
        SWEEP   (map-field (psf-section psf "SWEEP"))
        TRACE   (map-field (psf-section psf "TRACE"))
        params (into {} (cons {(first (keys (SWEEP "SWEEP"))) nil}
                  (map (fn [tr]
                        (let [types (get (TYPE "TYPE") (second tr))
                              p (filter #(and (not= % "key")(not= % "master")) (keys types))]
                          {(first tr) (if (> (depth types) 1)
                                          (zipmap p (repeat (count p) nil))
                                          nil)}))
                       (TRACE "TRACE"))))
        VALUE   (map-value (drop 2 (psf-section psf "VALUE")) params)]
    (into {} [HEADER TYPE SWEEP TRACE VALUE])))


;(def psf     (psf-bnf (slurp "./resources/noise3.noise")))
;(def psf     (psf-bnf (slurp "./resources/noise2.noise")))
;(def psf     (psf-bnf (slurp "./resources/dc2.dc")))

(def psf-map (map-psf "./resources/dc2.dc"))
;(def psf-map (map-psf "./resources/noise2.noise"))

(defn -main [& args] 
  
)
