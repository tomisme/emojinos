(ns emojinos.ui.frame
  (:require
   [re-frame.core :refer [dispatch dispatch-sync subscribe
                          reg-event-db reg-sub]]
   [reanimated.core :as anim]
   [emojinos.ui.elements :as e]
   [emojinos.game :as g]))

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
  [game-state {:keys [player idx x y]}]
  (let [emoji (get-in game-state [:p1 :hand idx])
        {:keys [rules board]} game-state
        new-board (conj board [emoji x y (= :p1 player)])]
    (-> game-state
        (update-in [:p1 :hand] remove-from-vec idx)
        (assoc :board new-board)
        (assoc :intermidiates
               (reductions g/apply-effect new-board (g/build-effects new-board rules))))))

(reg-event-db
 :you-place-tile
 (fn [db [_ idx x y]]
   (let [you (-> db :game :you-are)]
     (-> db
         (update :game place-tile {:player you :idx idx :x x :y y})))))

(defn you-place-tile!
  [{:keys [hand-index x y]}]
  (dispatch [:you-place-tile hand-index x y]))

(reg-event-db
 :finished-intermediates
 (fn [db _]
   (-> db
       (update :game dissoc :intermidiates)
       (update :game assoc :board (last (-> db :game :intermidiates))))))

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

(reg-sub
 :intermidiates
 (fn [db _]
   (-> db :game :intermidiates)))

(defn animated-board-timeline [intermediates]
  (into [anim/timeline]
        (interpose 1000
                   (for [board intermediates]
                     (e/board-el {:board board
                                  :place-tile! #()})))))

(defn board-component
  []
  (let [board @(subscribe [:board])
        intermediates @(subscribe [:intermidiates])]
    [:div
     (if (not intermediates)
       (e/board-el {:board board
                    :place-tile! you-place-tile!})
       (conj (animated-board-timeline intermediates)
             #(dispatch [:finished-intermediates])))
     [:div
      "intermediates"
      [:pre [:code (str intermediates)]]]
     [:div
      "board"
      [:pre [:code (str board)]]]]))


(defn ui-component
  []
  [:div {:style {:margin 10}}
   [:div {:style {:margin "10px 0"}}
    [:button {:on-click #(dispatch [:initialize])}
     "reset all!"]
    [:button {:on-click #(dispatch [:pass-turn])}
     "pass turn"]]
   (e/player-zone-el
    (e/hand-el {:hand @(subscribe [:p2-hand])
                 :playable? false
                 :white? false})
    (e/points-el @(subscribe [:p2-points])))
   (e/player-zone-el
    (e/hand-el {:hand @(subscribe [:p1-hand])
                 :playable? true
                 :white? true})
    (e/points-el @(subscribe [:p1-points])))
   [board-component]
   (e/rules-editor-el
    (e/rules-el @(subscribe [:rules])))])

(defonce _
  (dispatch-sync [:initialize]))
