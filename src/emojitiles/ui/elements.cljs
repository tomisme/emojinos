(ns emojinos.ui.elements
  (:require
   [emojinos.game :refer [board-edges]]))

(defn- prevent-default [e]
  (.preventDefault e))

(defn tile-el
  [{:keys [emoji hand-index playable? target? on-drop!]}]
  [:div {:draggable playable?
         :on-drag-over prevent-default
         :on-drag-enter prevent-default
         :on-drag-start #(-> %
                             .-dataTransfer
                             (.setData "text/plain" hand-index))
         :on-drop (fn [e]
                    (when target?
                      (let [hand-index (-> e
                                           .-dataTransfer
                                           (.getData "text/plain")
                                           int)]
                        (do
                         (prevent-default e)
                         (on-drop! hand-index)))))
         :style {:display "flex"
                 :user-select "none"
                 :-moz-user-select "none"
                 :cursor (if playable? "grab")
                 :opacity (if target? 0.1 1)
                 :background "white"
                 :justify-content "center"
                 :border "2px solid"
                 :box-shadow "inset -2px -5px 0 #cab6b6"
                 :border-radius 15
                 :width 70
                 :height 70}}
   [:div {:style {:margin-top 9
                  :align-items "center"
                  :font-size 40}}
    emoji]])

(defn board-el
  [{:keys [board place-tile!]}]
  (let [tile-x-offsets (map (fn [[_ dx _]] dx) board)
        tile-y-offsets (map (fn [[_ _ dy]] dy) board)
        min-dx (js/Math.abs (apply min tile-x-offsets))
        max-dx (apply max tile-x-offsets)
        min-dy (js/Math.abs (apply min tile-y-offsets))
        max-dy (apply max tile-y-offsets)
        board-width (+ 3 min-dx max-dx)
        board-height (+ 3 min-dy max-dy)
        ->px #(* % 78)]
    (into [:div {:style {:position "relative"
                         :width (->px board-width)
                         :height (->px board-height)}}]
          (into (for [[emoji dx dy] board]
                  [:div {:style {:position "absolute"
                                 :left (+ (->px dx)
                                          (->px (inc min-dx)))
                                 :bottom (+ (->px dy)
                                            (->px (inc min-dy)))}}
                   (tile-el {:emoji emoji})])
                (for [[dx dy] (board-edges board)]
                  [:div {:style {:position "absolute"
                                 :left (+ (->px dx)
                                          (->px (inc min-dx)))
                                 :bottom (+ (->px dy)
                                            (->px (inc min-dy)))}}
                   (tile-el {:target? true
                             :on-drop! #(place-tile! % dx dy)})])))))

(defn hand-el
  [hand playable?]
  (into [:div {:style {:display "flex"
                       :margin-bottom 15}}]
        (map-indexed (fn [idx emoji]
                      [:div {:style {:margin 2}}
                       (tile-el {:emoji emoji
                                 :playable? playable?
                                 :hand-index idx})])
                     hand)))

(defn points-el
  [i]
  [:div {:style {:font-size 50
                 :font-weight 600
                 :margin "10px 20px 0 20px"}}
   i])

(defn player-zone-el
  [& children]
  (into [:div {:style {:display "flex"
                       :justify-content "space-between"}}]
        children))
