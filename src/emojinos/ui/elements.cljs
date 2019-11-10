(ns emojinos.ui.elements
  (:require
   [emojinos.game :as game]))

(defn- prevent-default
  [e]
  (.preventDefault e))

(defn- hand-tile-drag-start-fn
  [hand-index]
  (fn [e]
    (-> e
        .-dataTransfer
        (.setData "text/plain" hand-index))))

(defn- target-on-drop-fn
  [on-drop!]
  (fn [e]
    (let [idx (-> e
                  .-dataTransfer
                  (.getData "text/plain")
                  int)]
      (do
       (prevent-default e)
       (on-drop! idx)))))

(defn tile-el
  [{:keys [emoji hand-index
           playable? target? white? blank?
           on-drop!]}]
  [:div {:draggable playable?
         :on-drag-over prevent-default
         :on-drag-enter prevent-default
         :on-drag-start (when playable?
                          (hand-tile-drag-start-fn hand-index))
         :on-drop (when target?
                    (target-on-drop-fn on-drop!))
         :style {:display "flex"
                 :user-select "none"
                 :-moz-user-select "none"
                 :cursor (when playable? "grab")
                 :opacity (cond
                            target? 0.1
                            blank? 0.25
                            :else 1)
                 :background (if white? "white" "#262626")
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
  (let [x-positions (map (fn [[_ x _]] x) board)
        y-positions (map (fn [[_ _ y]] y) board)
        max-left (js/Math.abs (apply min x-positions))
        max-right (apply max x-positions)
        max-down (js/Math.abs (apply min y-positions))
        max-up (apply max y-positions)
        board-width (+ 3 max-left max-right)
        board-height (+ 3 max-down max-up)
        ->px #(* % 78)]
    (into [:div {:style {:position "relative"
                         :width (->px board-width)
                         :height (->px board-height)}}]
          (into (for [[emoji x y white?] board]
                  [:div {:style {:position "absolute"
                                 :left (+ (->px x)
                                          (->px (inc max-left)))
                                 :bottom (+ (->px y)
                                            (->px (inc max-down)))}}
                   (tile-el {:emoji emoji
                             :white? white?})])
                (for [[x y] (game/targets board)]
                  [:div {:style {:position "absolute"
                                 :left (+ (->px x)
                                          (->px (inc max-left)))
                                 :bottom (+ (->px y)
                                            (->px (inc max-down)))}}
                   (tile-el {:target? true
                             :on-drop! (fn [hand-index]
                                         (place-tile! {:hand-index hand-index
                                                       :x x
                                                       :y y}))})])))))

(defn hand-el
  [{:keys [hand playable? white?]}]
  (into [:div {:style {:display "flex"
                       :margin-bottom 15}}]
        (map-indexed (fn [idx emoji]
                      [:div {:style {:margin 2}}
                       (tile-el {:emoji emoji
                                 :playable? playable?
                                 :hand-index idx
                                 :white? white?})])
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

(defn rules-symbol-tile-el
  [{:keys [base overlays]}]
  [:div
   (when base
     (tile-el base))
   (when overlays
     (into [:div {:style {:position "relative"}}]
           (for [[overlay transformation] overlays]
             [:div {:style {:transform "scale(0.5)"
                            :position "absolute"
                            :left 35
                            :bottom 0}}
              (tile-el overlay)])))])


(defn rules-editor-el
  [& children]
  [:div {:style {:margin-top 20}}
   [:div {:style {:margin-bottom 10}}
    "Game rules:"]
   ;; 'add' buttton
   (into [:div {:style {:display "flex"}}]
         children)])

(defn rules-el
  [pairs]
  (let [vec->tiles (fn [tile-vec]
                     (map #(into [:div {:style {:margin 2}}]
                                 [(tile-el {:emoji %
                                            :white? true
                                            :blank? (not %)})])
                          tile-vec))
        pair-el (fn [[left right]]
                  [:div {:style {:display "flex"}}
                   (vec->tiles left)
                   [:div {:style {:font-size 60
                                  :margin "0 15px"}}
                    "→"]
                   (vec->tiles right)])]
    (into [:div]
          (map pair-el pairs))))
