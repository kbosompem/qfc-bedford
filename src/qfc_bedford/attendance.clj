(ns qfc-bedford.attendance
  (:require  [cheshire.core :refer :all]
             [clojure.data.csv :as csv]
             [clojure.set :refer [union]]  
             [clojure.edn :as edn]             
             [clojure.string :as str]   
             [dk.ative.docjure.spreadsheet :refer :all]
             [clojure.java.io :as io]))

(def midweek-service 
  "A set of phrases that identify Midweek Services in the UD-OLGC Database" 
  #{"MIDWEEK SERVICE"})

(def sunday-service 
  "A set of phrases that identify Sunday Services in the UD-OLGC Database"
  #{" SUNDAY 1ST SERVICE" " SUNDAY SERVICE" " SUNDAY 2ND SERVICE"})

(def sunday-and-midweek 
  "A set of phrases that identify Sunday and Midweek Services in the UD-OLGC Database"
  (union midweek-service sunday-service))

(def config 
 "A configuration hashmap. To be stored in an edn file in application directory" 
 (edn/read-string (slurp "configure.edn")))

(def attkeys
  "Column headings for extracting information in the UD-OLGC Database's AttendanceData table.  
   These columns are configurable in the configuration file. No defaults in the application."
  (or (:columns config) []))

(def att-url-root
  "The root URL for the attendance data  rest endpoint. Configurable in the config file." 
  (or (:attendance-url-root config) ""))

(def att-url-suffix 
  "The suffix of URL for the attendance data rest endpoint. Configurable in the config file." 
  (or (:attendance-url-suffix config) ""))

(defn url-encode
  "Escapes illegal characters in a URL. For example changes spaces to %20"
  [s]
  (java.net.URLEncoder/encode s "UTF-8"))

(defn get-attendance-data
  "For a given branch name, calls the attendance API endpoint. This returns all attendance records 
   for the selected branch."  
  [branch]
  (let [url (str att-url-root (url-encode branch) att-url-suffix)]  
    (->  url
         slurp
         (parse-string keyword))))
              
(defn get-midweek-services 
  "Extracts only midweek service attendance records from an attendance dataset."
  [attendance]
  (filter (midweek-service (:TYPE_x0020_OF_x0020_SERVICE  %) attendance)))

(defn get-sunday-services 
  "Extracts only sunday service attendance records from an attendance dataset." 
  [attendance]
  (filter (sunday-service (:TYPE_x0020_OF_x0020_SERVICE  %) attendance)))

(defn get-other-services 
  "Extracts special service attendance records from an attendance dataset." 
  [attendance]
  (filter (not (sunday-and-midweek (:TYPE_x0020_OF_x0020_SERVICE  %)) attendance)))

(defn gen-data
  "Formats data in the docjure format necessary for generating the Excel Spreadsheet outputs."  
  ([coll] (gen-data coll attkeys)) 
  ([coll keys]
   (vec (cons (vec (map str keys)) (mapv #(vec (vals (select-keys % keys))) coll)))))

(defn create-spreadsheet 
  "Create Spreadsheet" 
  [filename coll & args]
  (let [params (merge {} (apply hash-map args))
        wb (create-workbook (params :sheetname)
                            (vec (cons (vec (map str attkeys)) (mapv #(vec (vals (select-keys % attkeys))) coll))))
        sheet (select-sheet (params :sheetname) wb)
        header-row (first (row-seq sheet))]
    (set-row-style! header-row (create-cell-style! wb {:background (params :background),:font (params :font)}))
    (save-workbook! filename wb)))

(defn format-sheet-header [wb sheetname]
  (->> wb 
    (select-sheet sheetname) 
    row-seq 
    first 
    (#(set-row-style! % (create-cell-style! wb {:background :blue ,:font {:bold true}})))))


(defn gen-workbook! 
  "Generates an Excel Spreadsheet with 3 tabs capturing attendance data for midweek services, sunday 
   services and other services. You can provide a path to store the file(s) in" 
  ([branch] (gen-workbook! "" branch)) 
  ([path branch]
   (let [att-data (get-attendance-data branch)
         filename (str path (str/lower-case (str/replace branch " " "-"))
                       "-attendance.xlsx")
         _  (clojure.java.io/make-parents filename)
         wb (create-workbook 
             "midweek" (gen-data (get-midweek-services att-data))
             "sundays" (gen-data (get-sunday-services att-data))
             "others"  (gen-data (get-other-services att-data)))
         _  (map #(format-sheet-header wb %) (sheet-seq wb))]
     (save-workbook! filename))))
                     
