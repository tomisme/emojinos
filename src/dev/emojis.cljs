(ns dev.emojis
  (:require
   [devcards.core]
   [emojinos.game :as game]
   [emojinos.ai :as ai]
   [emojinos.ui.elements :refer [tile-el hand-el board-el rules-el]]
   [emojinos.ui.frame :refer [ui-component]])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(defcard-rg tiles
  (into [:div {:style {:display "flex"
                       :flex-wrap "wrap"}}]
        (for [x [
                 "💓" "💞" "🍇" "🌱" "🌧️" "🌻" "🍄" "🔥" "🌷" "🍚"
                 "🥰" "🐦" "🌏" "🐻" "🍔" "⚽" "⏳" "🧱" "💣" "📺"
                 "🌼" "🌾" "🌈" "🏥" "⛩" "🗿" "🤔" "🐥" "🕷"
                 "💓" "💞" "🍇" "🌱" "🌧️" "🌻" "🍄" "🔥" "🌷" "🍚"
                 "🌼" "🌾" "🌈" "🏥" "⛩" "🗿" "🤔" "🐥" "🕷"]]

          [:div {:style {:margin 5}}
           (tile-el {:emoji x
                     :white? (rand-nth [true false])})])))

(def s1
  {:board #{["🌧️" 0 0]
            ["🌱" 0 1]
            ["🌷" 0 -1]
            ["🌱" -1 0]
            ["🌱" 1 0]
            ["🔥" -2 0]}
   :p1 ["🍚" "💞" "🍇" "🌱" "🌱"]
   :p2 ["🌻" "🍇" "🌱" "🌱" "🌱"]
   :bag #{"🌧️" "🌻" "🍄"}
   :rules [
           [["🌈" "🌱"] [nil "🍇"]]
           [["🔥" "🌱" "🌧️"] [nil "🌻" "🌈"]]]})

(defcard state1 s1)

(defcard state1-details
  {'targets (game/targets (:board s1))})

(defcard-rg state1-board-render
  [:div
   (hand-el {:hand (:p2 s1)
             :playable? false
             :white? false})
   (hand-el {:hand (:p1 s1)
             :playable? true
             :white? true})
   (board-el {:board (:board s1)})])

(defcard-rg state1-rules-render
  (rules-el (:rules s1)))

(defcard state1-rules-test-details
  (let [board (:board s1)
        rules (:rules s1)]
    {:rules rules
     :board board

     "the tile at [-2 0]"
     (game/get-tile-at -2 0 board)

     "neighbors of [-1 0]"
     (game/get-neighbors -1 0 board)

     "effects chain"
     (game/build-effects-chain board rules)

     "resolved board"
     (game/resolve-board board rules)}))

(defcard-rg state1-board-post-rules-render
  (board-el {:board (game/resolve-board (:board s1) (:rules s1))}))

(defcard-rg frame-render
  [ui-component])
