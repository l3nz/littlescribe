(ns littlescribe.core-test
  (:require [clojure.test :refer :all]
            [littlescribe.core :refer :all]))

(deftest headername-test
  (testing ""
    (are [x y]
         (= (header-name x) y)
      "pippo" :pippo
      "Pippo" :pippo
      "Pippo Pluto" :pippo-pluto)))

(deftest csvtable-test
  (testing ""
    (are [x y]
         (= (csv->map x) y)

      ; Src
      [["A" "B"]
       ["1" "2"]
       ["3" "4"]]
      ; out
      [{:a "1" :b "2"}
       {:a "3" :b "4"}])))

