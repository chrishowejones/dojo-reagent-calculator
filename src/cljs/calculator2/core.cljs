(ns calculator2.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

;; -------------------------
;; Views

(defonce calc (atom {:total 0 :display 0 :oper (partial +)}))

(defn handle-number-btn [val]
  (fn [] (swap! calc #(assoc % :display val))))

(defn handle-operator [operator]
  (let [operation (partial operator (:display @calc))]
    (fn [] (swap! calc #(assoc % :oper operation)))))

(defn handle-clear []
  (fn [] (swap! calc #(assoc % :display 0))))

(defn handle-equals []
  (let [oper (:oper @calc)
        display (:display @calc)
        result (oper display)]
   (fn [] (swap! calc #(assoc % :display result)))))

(defn top-row []
  [:div.top-row
   [:input {:type "button"
            :value 7
            :onClick (handle-number-btn 7)}]
   [:input {:type "button"
            :value 8
            :onClick (handle-number-btn 8)}]
   [:input {:type "button"
            :value 9
            :onClick (handle-number-btn 9)}]])

(defn middle-row []
  [:div.middle-row
   [:input {:type "button"
            :value 4
            :onClick (handle-number-btn 4)}]
   [:input {:type "button"
            :value 5
            :onClick (handle-number-btn 5)}]
   [:input {:type "button"
            :value 6
            :onClick (handle-number-btn 6)}]])

(defn bottom-row []
  [:div
   [:input {:type "button"
            :value 1
            :onClick (handle-number-btn 1)}]
   [:input {:type "button"
            :value 2
            :onClick (handle-number-btn 2)}]
   [:input {:type "button"
            :value 3
            :onClick (handle-number-btn 3)}]])

(defn operator-row []
  [:div
   [:input {:type "button"
            :value "+"
            :onClick (handle-operator +)}]
   [:input {:type "button"
            :value "-"
            :onClick (handle-operator -)}]
   [:input {:type "button"
            :value "/"
            :onClick (handle-operator /)}]
   [:input {:type "button"
            :value "*"
            :onClick (handle-operator *)}]
   [:input {:type "button"
            :value "="
            :onClick (handle-equals)}]
   [:input {:type "button"
            :value "C"
            :onClick (handle-clear)}]])


(defn display [calc]
  [:div
   [:input {:type "text"
            :size "20"
            :value (:display calc)
            :readOnly true}]])


(defn home-page []
  [:div [:h2 "Welcome to calculator2"]
   (display @calc)
   (top-row)
   (middle-row)
   (bottom-row)
   (operator-row)])


(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))


;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
