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

(defn map-value [psf-values map-values]
  (if (empty? psf-values)
    {"VALUE" map-values}
    (let [field (map-field (first psf-values))
          parameter (first (keys field))
          values (vals field)]
      (map-value (rest psf-values)
                 (into {} 
                    (map (fn [kv]
                           (if (= (first kv) parameter)
                              {parameter (cons values (second kv))}
                              {(first kv) (second kv)}))
                         map-values))))))

(defn psf-section [psf-file psf-section]
  (first (filter #(= (second %) psf-section) psf-file)))

(defn depth [hm]
  (if (map? hm) 
      (inc (apply max (map depth (vals hm)))) 
      0))

(defn map-psf [file-name]
  (let [psf     (psf-bnf (slurp file-name))
        HEADER  (map-field (psf-section psf "HEADER"))
        TYPE    (map-field (psf-section psf "TYPE"))
        SWEEP   (map-field (psf-section psf "SWEEP"))
        TRACE   (map-field (psf-section psf "TRACE"))

        params (into {} (cons {(first (keys (SWEEP "SWEEP"))) nil}
                              (map (fn [tr]
                                    (let [types (get (TYPE "TYPE") (second tr))]
                                      {(first tr) (if (> (depth types) 1)
                                                      (zipmap (keys types) 
                                                              (repeat (count (keys types)) nil))
                                                      nil)}))
                                   (TRACE "TRACE"))))


        VALUE   (map-value (drop 2 (psf-section psf "VALUE"))
                           (zipmap params (repeat (count params) [])))
        psf-map (into {} [HEADER TYPE SWEEP TRACE VALUE])]
    psf-map))


(def psf     (psf-bnf (slurp "./resources/noise2.noise")))
(def psf     (psf-bnf (slurp "./resources/dc2.dc")))

(def HEADER  (map-field (psf-section psf "HEADER")))
(def TYPE    (map-field (psf-section psf "TYPE")))
(def SWEEP   (map-field (psf-section psf "SWEEP")))
(def TRACE   (map-field (psf-section psf "TRACE")))

(def params (into {} (cons {(first (keys (SWEEP "SWEEP"))) nil}
  (map (fn [tr]
        (let [types (get (TYPE "TYPE") (second tr))]
          {(first tr) (if (> (depth types) 1)
                          (zipmap (keys types) (repeat (count (keys types)) nil))
                          nil)}))
       (TRACE "TRACE")))))

(defn map-value [values map-values]
    (if values
      (let [field (map-field values)
            id (first (keys field))
            value (first (vals field))
            map-val (if (map? (map-values id))
                        (reduce (fn [map-val enum-id]
                                  (assoc-in map-val [id (second enum-id)] (cons )))
                                map-values 
                                (map #(vector %1 %2) (keys (map-values id))
                                                     (first (vals (value)))))
          (assoc-in map-values [id] (cons (first value) 
                                          (map-values id))))]
      (map-value (rest values) map-val))
      {"VALUES" (into {} values)}))

(def value (psf-section psf "VALUE"))
(def elem (second (drop 2 value)))
(map-value [elem] params)


(def VALUE   (map-value (drop 2 (psf-section psf "VALUE"))
                   (zipmap params (repeat (count params) []))))
;
;(def psf-map (into {} [HEADER TYPE SWEEP TRACE VALUE]))

;(def psf-map (map-psf "./resources/dc2.dc"))
;(def psf-map (map-psf "./resources/noise2.noise"))

(defn -main [& args] 
  
  )
