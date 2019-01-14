(ns emojitiles.dev.emojis
  (:require
   [devcards.core])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(def all-emojis
  ["💓"
   "💞"
   "🍇"
   "🌱"
   "🌧️"
   "🌻"
   "🍄"
   "🔥"
   "🌷"
   "🍚"])

(defcard all-emojis all-emojis)

(defn tile-el
  ([x]
   (tile-el x false))
  ([x fake?]
   [:div {:style {:display "flex"
                  :opacity (if fake? 0.2 1)
                  :background "white"
                  :justify-content "center"
                  :border "2px solid"
                  :box-shadow "inset -3px -6px 0 #cab6b6"
                  :border-radius 20
                  :width 100
                  :height 100}}
    [:div {:style {:margin-top "0.2em"
                   :display "flex"
                   :align-items "center"
                   :font-size #_60 20}}
     x]]))


(defcard-rg tiles
  (into [:div {:style {:display "flex"
                       :flex-wrap "wrap"}}]
        (for [x all-emojis]
          [:div {:style {:margin 5}}
           (tile-el x)])))

;; tile = [str x-offset y-offset]

(def s1
  {:board #{["🍚" 0 0]
            ["🌷" 0 -1]
            ["🍇" -1 0]
            ["🍇" -2 0]}
   :p1 ["🍚" "💞" "🍇"]
   :p2 ["🌻" "🍇"]
   :bag ["🌧️" "🌻" "🍄"]})

(defn filled-positions
  [board]
  (into #{}
        (map (fn [[_ x y]]
               [x y])
             board)))

(defn pos-neighbors
  [x y]
  #{[(inc x) y]
    [x (inc y)]
    [(dec x) y]
    [x (dec y)]})

(defn board-edges
  [board]
  (let [filled (filled-positions board)]
    (reduce
     (fn [board-edges [_ x y]]
       (loop [to-check (seq (pos-neighbors x y))
              empty-neighbors #{}]
           (if (empty? to-check)
             (into board-edges empty-neighbors)
             (let [checking (first to-check)]
               (recur (rest to-check)
                      (if (contains? filled checking)
                        empty-neighbors
                        (conj empty-neighbors checking)))))))
     #{}
     board)))

(defcard board-details
  (let [b (:board s1)]
    {'board b
     'filled-positions (filled-positions b)
     'board-edges (board-edges b)}))

(defcard-rg boardtest
  (let [b (:board s1)
        tile-x-offsets (map (fn [[_ dx _]] dx) b)
        tile-y-offsets (map (fn [[_ _ dy]] dy) b)
        min-dx (js/Math.abs (apply min tile-x-offsets))
        max-dx (apply max tile-x-offsets)
        min-dy (js/Math.abs (apply min tile-y-offsets))
        max-dy (apply max tile-y-offsets)
        board-width (+ 3 min-dx max-dx)
        board-height (+ 3 min-dy max-dy)
        tile-px 105]
    (into [:div {:style {:position "relative"
                         :background "grey"
                         :width (* tile-px board-width)
                         :height (* tile-px board-height)}}]
          (into (for [[tile dx dy] b]
                  [:div {:style {:position "absolute"
                                 :left (+ (* dx tile-px)
                                          (* (inc min-dx) tile-px))
                                 :bottom (+ (* dy tile-px)
                                            (* (inc min-dy) tile-px))}}
                   (tile-el (str "(" dx "," dy ") " tile))])
                (for [[dx dy] (board-edges b)]
                  [:div {:style {:position "absolute"
                                 :left (+ (* dx tile-px)
                                          (* (inc min-dx) tile-px))
                                 :bottom (+ (* dy tile-px)
                                            (* (inc min-dy) tile-px))}}
                   (tile-el (str "(" dx "," dy ")") true)])))))
