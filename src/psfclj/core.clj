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

(defn parse-field [field]
  (cond (= (first field) :section)
          {(second field) (into {} (map parse-field (drop 2 field)))}
        (= (first field) :attribute)
          {(second field) 
           (if (> (count field) 3)
               (into {} (map parse-field (drop 2 field)))
               (if (<= (count (last field)) 2)
                   (last (last field))
                   (rest (last field))))}
        (or (= (first field) :prop)
            (= (first field) :struct))
          (into {} (map parse-field (rest field)))
        (or (= (first field) :values)
            (= (first field) :types)
            (= (first field) :unit))
          {(name (first field)) 
           (if (> (count field) 2)
            (rest field) (first (rest field)))}
        :else
          {}))

(defn parse-value [values parameters]
  (into {} 
    (map (fn [param]
          (let [param-value (map #(first (drop 2 %))
                                 (filter #(= (second %) (first param)) 
                                 values))]
            {(first param) 
             (if (map? (second param))
                 (into {} (map #(hash-map %1 (map read-string %2))
                               (keys (second param))
                               (transpose (map rest param-value))))
                 (map #(read-string (second %)) param-value))}))
         parameters)))

(defn parse-psf [psf]
  (let [HEADER  (parse-field (psf-section psf "HEADER"))
        TYPE    (parse-field (psf-section psf "TYPE"))
        SWEEP   (parse-field (psf-section psf "SWEEP"))
        TRACE   (parse-field (psf-section psf "TRACE"))
        params (into {} (cons {(first (keys (SWEEP "SWEEP"))) nil}
                  (map (fn [tr]
                        (let [types (get (TYPE "TYPE") (second tr))
                              p (filter #(and (not= % "key")(not= % "master")) (keys types))]
                          {(first tr) (if (> (depth types) 1)
                                          (zipmap p (repeat (count p) nil))
                                          nil)}))
                       (TRACE "TRACE"))))
        VALUE   (parse-value (drop 2 (psf-section psf "VALUE")) params)]
    (into {} [HEADER TYPE SWEEP TRACE VALUE])))

(defn -main [& args] 
  (parse-psf (psf-bnf (slurp file-name)))
)
