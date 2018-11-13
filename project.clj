(defproject qfc-bedford "0.1.0"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [dk.ative/docjure "1.13.0"]
                 [hiccup "1.0.5"]   
                 [cheshire "5.8.1"]]

  :plugins [[lein-codox "0.9.6"]
            [lein-gorilla "0.4.0"]
            [lein-ancient "0.6.15"]]

  :uberjar-name "qfcbedford.jar"
  :aot [qfc-bedford.core]
  :main qfc-bedford.core)


