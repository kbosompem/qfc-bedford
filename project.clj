(defproject qfc-bedford "0.1.0"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [dk.ative/docjure "1.13.0"]
                 [com.draines/postal "2.0.3"]
                 [hiccup "1.0.5"]   
                 [camel-snake-kebab "0.4.0"]
                 [cheshire "5.8.1"]]

  :uberjar-name "qfcbedford.jar"
  :aot [qfc-bedford.core]
  :main qfc-bedford.core)


