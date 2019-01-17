(ns emojinos.ui.frame
  (:require
   [re-frame.core :refer [dispatch-sync reg-event-db reg-sub subscribe]]
   [emojinos.ui.elements :as el]))

(reg-event-db
 :initialize
 (fn []
   {:board #{["🌱" 0 0]}
    :p1-hand ["🍚"]
    :p2-hand ["🍇"]}))

(reg-sub
 :board
 (fn [db _]
   (:board db)))

(reg-sub
 :p1-hand
 (fn [db _]
   (:p1-hand db)))

(reg-sub
 :p2-hand
 (fn [db _]
   (:p2-hand db)))

(defn ui-component
  []
  [:div
   (el/hand-el @(subscribe [:p2-hand]) false)
   (el/board-el @(subscribe [:board]))
   (el/hand-el @(subscribe [:p1-hand]) true)])

(dispatch-sync [:initialize])
