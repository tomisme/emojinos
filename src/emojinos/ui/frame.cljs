(ns emojinos.ui.frame
  (:require
   [re-frame.core :refer [dispatch dispatch-sync subscribe
                          reg-event-db reg-sub]]
   [emojinos.ui.elements :as el]
   [emojinos.game :as game]))

(reg-event-db
 :initialize
 (fn []
   {:game {:you-are :p1
           :active-player :p1
           :board #{["🌱" 0 0 false]}
           :p1 {:hand ["🌱" "🌱" "🔥" "🔥" "🌧️" "🌧️"]
                :points 0}
           :p2 {:hand []
                :points 0}
           :rules [
                   [["🌈" "🌱"] [nil "🍇"]]
                   [["🔥" "🌱" "🌧️"] [nil "🌻" "🌈"]]]}}))

(defn remove-from-vec
  "Returns a new vector with the element at 'index' removed.

  (remove-from-vec [:a :b :c] 1)  =>  [:a :c]"
  [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))

(defn place-tile
  [state {:keys [player idx x y]}]
  (let [emoji (get-in state [:p1 :hand idx])
        rules (:rules state)]
    (-> state
        (update-in [:p1 :hand] remove-from-vec idx)
        (update :board conj [emoji x y (= :p1 player)])
        (update :board game/resolve-board rules))))

(reg-event-db
 :you-place-tile
 (fn [db [_ idx x y]]
   (let [you (-> db :game :you-are)
         opp (if (= :p1 you) :p2 :p1)]
     (-> db
         (update :game place-tile {:player you :idx idx :x x :y y})))))

(defn you-place-tile!
  [{:keys [hand-index x y]}]
  (dispatch [:you-place-tile hand-index x y]))

(reg-sub
 :board
 (fn [db _]
   (-> db :game :board)))

(reg-sub
 :rules
 (fn [db _]
   (-> db :game :rules)))

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
  [:div {:style {:margin 10}}
   [:div {:style {:margin "10px 0"}}
    [:button {:on-click #(dispatch [:initialize])}
     "reset all!"]
    [:button {:on-click #(dispatch [:pass-turn])}
     "pass turn"]]
   (el/player-zone-el
    (el/hand-el {:hand @(subscribe [:p2-hand])
                 :playable? false
                 :white? false})
    (el/points-el @(subscribe [:p2-points])))
   (el/player-zone-el
    (el/hand-el {:hand @(subscribe [:p1-hand])
                 :playable? true
                 :white? true})
    (el/points-el @(subscribe [:p1-points])))
   (el/board-el {:board @(subscribe [:board])
                 :place-tile! you-place-tile!})
   (el/rules-editor-el
    (el/rules-el @(subscribe [:rules])))])

(defonce _
  (dispatch-sync [:initialize]))
