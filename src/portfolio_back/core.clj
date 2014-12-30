(ns portfolio-back.core
  (:require [clj-http.client :as client]
            [clj-time.core :as time]
            [clojure.set :as set]
            [clojure-csv.core :as csv]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class :main true))

(def #^{:private true} +base-url+ "http://ichart.finance.yahoo.com/table.csv?s=%s&g=d&a=%d&b=%d&c=%d&d=%d&e=%d&f=%d")

(defn- parse-date [dt]
  (map #(Integer/parseInt %) (.split dt "-")))

(defn- get-full-url
  "Construct the complete URL given the params"
  [y1 m1 d1 y2 m2 d2 sym]
  (let [start (time/date-time y1 m1 d1)
        end (time/date-time y2 m2 d2)]
    (format +base-url+
            sym
            (dec (time/month start))
            (time/day start)
            (time/year start)
            (dec (time/month end))
            (time/day end)
            (time/year end))))

(defn- get-url [url]
  "Get URL"
  (client/get url))

(defn- fetch-historical-data
  "Fetch historical prices from Yahoo! finance for the given symbols between start and end"
  [start end syms]
  (let [[y1 m1 d1]   (parse-date start)
        [y2 m2 d2]   (parse-date end)
        xf           (comp
                      (map (partial get-full-url y1 m1 d1 y2 m2 d2))
                      (map get-url)
                      (map :body)
                      (map csv/parse-csv))]
    (into [] xf syms)))

(defn- csv->columns [csvdata]
  "Return CSV data columns"
  (first csvdata))

(defn- csv->values [csvdata]
  "Return CSV data values"
  (rest csvdata))

(defn- positions
  [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x)
                    idx))
                coll))

(defn- cols->indices [columns fields]
  (positions (set fields) columns))

(defn- indices->elems [all-elems indices]
  (remove nil?
          (reduce
           (fn [so-far index]
             (conj so-far (nth all-elems index nil)))
           []
           indices)))

(defn fetch-fields
  "Fetch fields for symbols."
  [start end syms fields]
  (let [csv-data   (fetch-historical-data start end syms)
        data-cols  (csv->columns (first csv-data))
        indices    (cols->indices data-cols fields)
        selector   (fn [values]
                     (indices->elems values indices))
        seq-sel    (fn [points]
                     (map selector points))]
    (->> csv-data
         (map csv->values)
         (map seq-sel)
         (zipmap syms))))

(def cli-options
  [[nil "--cols" "Output columns"
    :parse-fn #(.split % ",")
    :default "Date"]])

(defn -main
  "The application's main function"
  [& args]
  (let [[start-d end-d cols & syms] args]
    (println (fetch-fields start-d end-d syms (.split cols ",")))))
