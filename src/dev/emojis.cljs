(ns dev.emojis
  (:require
   [devcards.core]
   [emojinos.core :refer [filled-positions board-edges]]
   [emojinos.ui.elements :refer [tile-el hand-el board-el]]
   [emojinos.ui.frame :refer [ui-component]])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(defcard-rg tile-test
  (into [:div {:style {:display "flex"
                       :flex-wrap "wrap"}}]
        (for [x ["💓" "💞" "🍇" "🌱" "🌧️" "🌻" "🍄" "🔥" "🌷" "🍚"]]
          [:div {:style {:margin 5}}
           (tile-el {:content x})])))

(def s1
  {:board #{["🍚" 0 0]
            ["🌷" 0 -1]
            ["🍇" -1 0]
            ["🍇" -2 0]}
   :p1 ["🍚" "💞" "🍇" "🌱" "🌱"]
   :p2 ["🌻" "🍇" "🌱" "🌱" "🌱"]
   :bag ["🌧️" "🌻" "🍄"]})

(defcard board-details
  (let [b (:board s1)]
    {'board b
     'filled-positions (filled-positions b)
     'board-edges (board-edges b)}))

(defcard-rg game-test
  [:div
   (hand-el (:p2 s1) false)
   (board-el (:board s1))
   (hand-el (:p1 s1) true)])

(defcard-rg frame-test
  [ui-component])
