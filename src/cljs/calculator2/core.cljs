(ns calculator2.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

;; -------------------------
;; Views

(defonce calc (atom {:total 0 :display "0" :oper (partial identity) :reset-display true}))

(defn- to-number [x]
  (js/parseFloat x))

(defn- get-number [key]
  (to-number (get @calc key)))

(defn- reset-display []
  (swap! calc #(update % :reset-display (fn [_] false)))
  "")

(defn- set-display-reset []
  (swap! calc update :reset-display (fn [_] true)))

(defn- get-display []
  (if (:reset-display @calc)
    (reset-display)
    (:display @calc)))

(defn handle-number-btn [val]
  (fn []
    (let [disp (get-display)]
     (swap! calc #(assoc % :display (str disp val))))))

(defn handle-equals []
  (let [oper (:oper @calc)
        display (get-number :display)
        result (oper display)]
    (fn []
      (swap! calc #(assoc % :display result :total 0 :oper (partial identity)))
      (set-display-reset))))

(defn handle-operator [operator]
  (let [oper      (:oper @calc)
        display   (get-number :display)
        total     (oper display)
        operation (partial operator total)]
    (fn []
      (swap! calc #(assoc % :total total :oper operation :reset-display true)))))

(defn handle-clear []
  (fn []
    (swap! calc #(assoc % :oper (partial identity) :total 0 :display "0"))))

(defn top-row []
  [:p
   [:input {:type "button"
            :class "btn"
            :value 7
            :onClick (handle-number-btn 7)}]
   [:input {:type "button"
            :class "btn"
            :value 8
            :onClick (handle-number-btn 8)}]
   [:input {:type "button"
            :class "btn"
            :value 9
            :onClick (handle-number-btn 9)}]])

(defn middle-row []
  [:p
   [:input {:type "button"
            :class "btn"
            :value 4
            :onClick (handle-number-btn 4)}]
   [:input {:type "button"
            :class "btn"
            :value 5
            :onClick (handle-number-btn 5)}]
   [:input {:type "button"
            :class "btn"
            :value 6
            :onClick (handle-number-btn 6)}]])

(defn bottom-row []
  [:p
   [:input {:type "button"
            :class "btn"
            :value 1
            :onClick (handle-number-btn 1)}]
   [:input {:type "button"
            :class "btn"
            :value 2
            :onClick (handle-number-btn 2)}]
   [:input {:type "button"
            :class "btn"
            :value 3
            :onClick (handle-number-btn 3)}]])

(defn zero-row []
  [:p
   [:input {:type "button"
            :class "btn"
            :value 0
            :onClick (handle-number-btn 0)}]])

(defn operator-row []
  [:div.btn-group-vertical {:style {:margin "10px"}}
   [:input {:type "button"
            :class "btn"
            :value "C"
            :onClick (handle-clear)}]
   [:input {:type "button"
            :class "btn"
            :value "+"
            :onClick (handle-operator +)}]
   [:input {:type "button"
            :class "btn"
            :value "-"
            :onClick (handle-operator -)}]
   [:input {:type "button"
            :class "btn"
            :value "/"
            :onClick (handle-operator /)}]
   [:input {:type "button"
            :class "btn"
            :value "*"
            :onClick (handle-operator *)}]
   [:input {:type "button"
            :class "btn"
            :value "="
            :onClick (handle-equals)}]])


(defn display [calc]
  [:p
   [:input {:type "text"
            :size "20"
            :value (:display calc)
            :readOnly true}]])


(defn home-page []
  [:div.container
   [:h3 "Calculator"]
   (display @calc)
   [:table
    [:td
     (top-row)
     (middle-row)
     (bottom-row)
     (zero-row)]
    [:td
     (operator-row)]]])


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
