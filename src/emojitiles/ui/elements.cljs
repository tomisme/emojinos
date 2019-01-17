(ns emojinos.ui.elements
  (:require
   [emojinos.core :refer [board-edges]]))

(defn- prevent-default [e]
  (.preventDefault e))

(defn tile-el
  [{:keys [content board-x board-y hand-index movable? edge?]}]
  [:div {:draggable movable?
         :on-drag-over prevent-default
         :on-drag-enter prevent-default
         :on-drag-start #(-> %
                             .-dataTransfer
                             (.setData "text/plain" hand-index))
         :on-drop #(let [hand-index (-> %
                                        .-dataTransfer
                                        (.getData "text/plain")
                                        int)]
                     (do
                      (prevent-default %)
                      (js/console.log hand-index board-x board-y)))
         :style {:display "flex"
                 :cursor (if movable? "grab")
                 :opacity (if edge? 0.1 1)
                 :background "white"
                 :justify-content "center"
                 :border "2px solid"
                 :box-shadow "inset -2px -5px 0 #cab6b6"
                 :border-radius 15
                 :width 70
                 :height 70}}
   [:div {:style {:margin-top 5
                  :display "flex"
                  :align-items "center"
                  :font-size 40}}
    [:div {:style {
                   :user-select "none"
                   :-moz-user-select "none"}}
     content]]])

(defn board-el
  [board]
  (let [tile-x-offsets (map (fn [[_ dx _]] dx) board)
        tile-y-offsets (map (fn [[_ _ dy]] dy) board)
        min-dx (js/Math.abs (apply min tile-x-offsets))
        max-dx (apply max tile-x-offsets)
        min-dy (js/Math.abs (apply min tile-y-offsets))
        max-dy (apply max tile-y-offsets)
        board-width (+ 3 min-dx max-dx)
        board-height (+ 3 min-dy max-dy)
        offset-px 78]
    (into [:div {:style {:position "relative"
                         :width (* offset-px board-width)
                         :height (* offset-px board-height)}}]
          (into (for [[tile dx dy] board]
                  [:div {:style {:position "absolute"
                                 :left (+ (* dx offset-px)
                                          (* (inc min-dx) offset-px))
                                 :bottom (+ (* dy offset-px)
                                            (* (inc min-dy) offset-px))}}
                   (tile-el {:content tile})])
                (for [[dx dy] (board-edges board)]
                  [:div {:style {:position "absolute"
                                 :left (+ (* dx offset-px)
                                          (* (inc min-dx) offset-px))
                                 :bottom (+ (* dy offset-px)
                                            (* (inc min-dy) offset-px))}}
                   (tile-el {:edge? true
                             :board-x dx
                             :board-y dy})])))))

(defn hand-el
  [hand yours?]
  (into [:div {:style {:display "inline-flex"
                       :margin "10px 0"}}]
        (map-indexed (fn [idx tile]
                      [:div {:style {:margin 2}}
                       (tile-el {:content tile
                                 :movable? yours?
                                 :hand-index idx})])
                     hand)))
