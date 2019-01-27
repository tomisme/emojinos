(ns emojinos.ui.frame
  (:require
   [re-frame.core :refer [dispatch dispatch-sync subscribe
                          reg-event-db reg-sub]]
   [emojinos.ui.elements :as el]
   [emojinos.game :as game]))

(reg-event-db
 :initialize
 (fn []
   {:game {:board #{["🌱" 0 0]}
           :p1 {:hand ["🍚" "🍄" "🌧️" "🔥" "🌷"]
                :points 0}
           :p2 {:hand ["🍇" "🍇" "🍇" "🍇" "🍇"]
                :points 0}}}))

(reg-event-db
 :place-tile
 (fn [db [_ player hand-index x y]]
   (update db :game game/place-tile player hand-index x y)))

(defn place-tile!
  [{:keys [hand-index x y]}]
  (dispatch [:place-tile :p1 hand-index x y]))

(reg-sub
 :board
 (fn [db _]
   (-> db :game :board)))

(reg-sub
 :p1-hand
 (fn [db _]
   (-> db :game :p1 :hand)))

(reg-sub
 :p2-hand
 (fn [db _]
   (-> db :game :p2 :hand)))

(reg-sub
 :p1-points
 (fn [db _]
   (-> db :game :p1 :points)))

(reg-sub
 :p2-points
 (fn [db _]
   (-> db :game :p2 :points)))

(defn ui-component
  []
  [:div
   [:button {:on-click #(dispatch [:initialize])}
    "reset state"]
   [:button {:on-click #(dispatch [:pass-turn])}
    "pass turn"]
   (el/player-zone-el
    (el/hand-el @(subscribe [:p2-hand]) false)
    (el/points-el @(subscribe [:p2-points])))
   (el/player-zone-el
    (el/hand-el @(subscribe [:p1-hand]) true)
    (el/points-el @(subscribe [:p1-points])))
   (el/board-el {:board @(subscribe [:board])
                 :place-tile! place-tile!})])

(defonce _
  (dispatch-sync [:initialize]))