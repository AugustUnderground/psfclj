(ns psfclj.core
  (:require [instaparse.core :as insta]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:gen-class))

(def psf-help (str "Usage: psfclj [options] <file>\n"
                   "Read a PSF Data from <file>.\n\n"
                   "The output will be written to stdout and "
                   "can be redirected into a file.\n"
                   "Options:\n"))

(def psf-cli-options
    [["-g" "--grammar BNF" 
      "BNF Context Free Grammar File"
      :default nil
      :id :grammar
      :validate [#(.exists (io/file %))
                 "Specified grammar file doesn't exist."]]
     ["-j" "--json"
      "Output in JSON Format"
      :id :json
      :default true]
     ["-c" "--csv"
      "Output values in CSV Format"
      :id :csv
      :default false]
     ["-h" "--help"
      :id :help]])

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
  (if values
    {"VALUE" 
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
           parameters))}
    {}))

(defn reduce-value [value-map & {:keys [parent-key flat-map] 
                                 :or   {parent-key nil flat-map {}}}]
  (if (empty? value-map)
    flat-map
    (let [[param value] (first value-map)
          param-key (if parent-key (str parent-key "." param) param)]
      (reduce-value (rest value-map) 
                    :parent-key parent-key 
                    :flat-map (if (map? value)
                                  (reduce-value value 
                                                :parent-key param 
                                                :flat-map flat-map)
                                  (into {} (concat {param-key value}
                                                   flat-map)))))))

(defn parse-psf [psf-content psf-bnf]
  (let [psf     (psf-bnf psf-content)
        _       (when (insta/failure? psf) (throw (Exception. "Parse Error.")))
        HEADER  (parse-field (psf-section psf "HEADER"))
        TYPE    (parse-field (psf-section psf "TYPE"))
        SWEEP   (parse-field (psf-section psf "SWEEP"))
        TRACE   (parse-field (psf-section psf "TRACE"))
        params (into {} (cons {(first (keys (SWEEP "SWEEP"))) nil}
                  (map (fn [tr]
                        (let [types (get (TYPE "TYPE") (second tr))
                              p (filter #(and (not= % "key") (not= % "master")) 
                                        (keys types))]
                          {(first tr) (if (> (depth types) 1)
                                          (zipmap p (repeat (count p) nil))
                                          nil)}))
                       (TRACE "TRACE"))))
        VALUE   (parse-value (drop 2 (psf-section psf "VALUE")) params)]
    (into {} [HEADER TYPE SWEEP TRACE VALUE])))

(defn write-csv [psf-map]
  (let [flat-map (reduce-value (psf-map "VALUE"))
        header (string/join "," (keys flat-map))
        values (map #(string/join "," %)
                    (transpose (vals flat-map)))]
    (string/join "\n" (cons header values))))

(defn exit [status & {:keys [msg] :or {msg ""}}]
  (if (not= status 0)
    (binding [*out* *err*] (println msg))
    (println msg))
  (System/exit status))


(defn -main [& args] 
  (let [opts (parse-opts args psf-cli-options)]
    (cond (opts :errors)
            (exit -2 :msg (str "ERRORS:" (opts :errors)))
          (contains? (opts :options) :help)
            (println psf-help (opts :summary))
          :else
            (let [file (if (> (count (opts :arguments)) 0)
                           (first (opts :arguments)) "")
                  psf-file (if (.exists (io/file file))
                               (slurp file)
                               (slurp *in*))
                  psf-bnf (insta/parser (if (opts :grammar) 
                                            (io/file (opts :grammar)) 
                                            (io/resource "psf.bnf")))
                  psf-map (try (parse-psf psf-file psf-bnf)
                               (catch Exception e (exit -1 :msg "Parse Error.")))]
              (cond (get-in opts [:options :csv])
                      (println (write-csv psf-map))
                    (get-in opts [:options :json])
                      (println (json/write-str psf-map))
                    :else
                      (exit -3 :msg "No output format specified."))))))
