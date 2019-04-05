(ns littlescribe.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [cljstache.core :as cljstache]
            [cli-matic.core :refer [run-cmd]])
  (:gen-class))

(defn gather-comments
  [body token]
  (let [realtoken (str "#-" token ":")
        lines (str/split-lines body)
        mylines (filter #(str/starts-with? % realtoken) lines)]
    (mapv #(-> %
               (subs (count realtoken))
               str/trim) mylines)))

(defn gather-single-comment
  [body token]
  (first (gather-comments body token)))

(defn remove-comments
  [body]
  (let [lines (str/split-lines body)
        myLines (filter #(not (str/starts-with? % "#-")) lines)]
    (str/join "\n" myLines)))

(defn render-mustache
  [data]
  ;(prn "D:" data)
  (cljstache/render (slurp (:template data)) data))

(defn template-to-mailing
  [data]

  (let [letter (render-mustache data)
        from (gather-single-comment letter "FROM")
        to (gather-comments letter "TO")
        cc (gather-comments letter "CC")
        bcc (gather-comments letter "BCC")
        subj (gather-single-comment letter "RE")
        body (remove-comments letter)]

    {:from from
     :to   to
     :cc   cc
     :bcc  bcc
     :subj subj
     :body body}))

(defn template-to-mailing-2
  [template data]
  (template-to-mailing (merge data {:template template})))

(defn encode-part
  [s]
  (cond
    (string? s)
    (-> s
        (java.net.URLEncoder/encode "UTF-8")
        (str/replace "+" "%20"))

    :else
    (encode-part (str/join "," s))))

(defn mail-link
  "Crea un link mailto:"
  [{:keys [to cc bcc subj body]}]
  (str "mailto:" (encode-part to)
       "?subject=" (encode-part subj)
       "&body=" (encode-part body)
       "&cc=" (encode-part cc)
       "&bcc=" (encode-part bcc)))

(defn send-mailing-emailclient
  [t]
  (let [link (mail-link t)]
    (shell/sh "open" link)))

; ---------

(defn header-name [s]
  (-> s
      str/lower-case
      (str/replace " " "-")
      keyword))

(defn csv->map [csv]
  (let [header (mapv header-name (first csv))
        rows (rest csv)]

    (mapv (fn [r]
            (zipmap header r))
          rows)))

(defn read_csv->map [filename]
  (csv->map
   (with-open [reader (io/reader filename)]
     (doall
      (csv/read-csv reader)))))

; -------


(defn process [{:keys [csv template]}]

  (let [data (read_csv->map csv)
        fnSend (fn [d] (send-mailing-emailclient
                        (template-to-mailing-2 template d)))
        _ (prn data)]
    (doall
     (map fnSend data))))

; (process {:csv "data/SomeData.csv" :template "test_template.txt"})


; ======= CLI =======

(def CONFIGURATION
  {:app         {:command     "littlescribe"
                 :description "Greeter"
                 :version     "0.0.1"}
   :global-opts []
   :commands    [{:command     "mail"
                  :description "Greets you"
                  :opts        [{:option "csv" :as "CSV file to read" :type :string :default :present}
                                {:option "template" :as "Template file to merge" :type :string :default :present}]
                  :runs        process}]})

(defn -main [& args]
  (run-cmd args CONFIGURATION))

