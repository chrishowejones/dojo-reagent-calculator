(ns calculator2.prod
  (:require [calculator2.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
