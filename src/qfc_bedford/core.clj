(ns qfc-bedford.core
  (:gen-class)
  (:require  [cheshire.core :refer :all]
             [clojure.data.csv :as csv]
             [clojure.edn :as edn]             
             [clojure.string :as str]   
             [dk.ative.docjure.spreadsheet :refer :all]
             [clojure.java.io :as io]))          


(def midweek-service #{"MIDWEEK SERVICE"})
(def sunday-service #{" SUNDAY 1ST SERVICE"})
(def sunday-and-midweek #{"MIDWEEK SERVICE" " SUNDAY 1ST SERVICE"})

(def config (edn/read-string (slurp "configure.edn")))
(def attkeys (or (:columns config) []))
(def att-url-root (or (:attendance-url-root config) ""))
(def att-url-suffix (or (:attendance-url-suffix config) ""))


(defn url-encode [s]
 (java.net.URLEncoder/encode s "UTF-8"))

(defn get-attendance-data [branch]
  (let [url (str att-url-root (url-encode branch) att-url-suffix)]  
    (->  url
         slurp
         (parse-string keyword))))
              

(defn get-midweek-services [attendance]
  (filter #(and
            (midweek-service (:TYPE_x0020_OF_x0020_SERVICE  %))
            (< 20170901 (:DATEOFSERVICENUMBER  %))) attendance))

(defn get-sunday-services [attendance]
  (filter #(and
            (sunday-service (:TYPE_x0020_OF_x0020_SERVICE  %))
            (< 20170901 (:DATEOFSERVICENUMBER  %))) attendance))

(defn get-other-services [attendance]
  (filter #(and
            (not (sunday-and-midweek (:TYPE_x0020_OF_x0020_SERVICE  %)))
            (< 20170901 (:DATEOFSERVICENUMBER  %))) attendance))

(defn gen-data 
  ([coll] (gen-data coll attkeys)) 
  ([coll keys]
   (vec (cons (vec (map str keys)) (mapv #(vec (vals (select-keys % keys))) coll)))))

(defn create-spreadsheet [filename coll & args]
  (let [params (merge {} (apply hash-map args))
        wb (create-workbook (params :sheetname)
                            (vec (cons (vec (map str attkeys)) (mapv #(vec (vals (select-keys % attkeys))) coll))))
        sheet (select-sheet (params :sheetname) wb)
        header-row (first (row-seq sheet))]
    (set-row-style! header-row (create-cell-style! wb {:background (params :background),:font (params :font)}))
    (save-workbook! filename wb)))

(defn gen-workbook! [branch]
 (let [att-data (get-attendance-data branch)
       filename (str (str/lower-case (str/replace branch " " "-"))
                     "-attendance.xlsx")]
  (save-workbook! filename
                  (create-workbook 
                   "midweek" (gen-data (get-midweek-services att-data))
                   "sundays" (gen-data (get-sunday-services att-data))
                   "others"  (gen-data (get-other-services att-data))))))

(defn -main [& args]
 (map gen-workbook! args))
