(ns qfc-bedford.mailer
  (:gen-class)
  (:require 
   [clojure.java.io :as io]
   [camel-snake-kebab.core :as csk]
   [postal.core :as mail]))



(defn attachpdf [filepath]
 [{:type :inline
   :content (-> filepath java.io.File.)
   :content-type "application/pdf"}
  {:type "text/html"
   :content (str "Sending " (-> filepath java.io.File. .getName))}])


(defn send-to-kindle [cfg email filepath]
  (let [r (try (mail/send-message cfg {:to email :from "kbosompem@gmail.com" :cc "kbosompem@gmail.com"
                                                   :body (attachpdf filepath)
                                                   :subject "Convert"})
            (catch Exception ex ex))]
    [email filepath r]))

(defn rename [from to]
 (.renameTo (java.io.File. from) (java.io.File. (->camel-snake-kebab to))))
