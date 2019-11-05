(ns dev.emojis
  (:require
   [devcards.core]
   [emojinos.game :as game]
   [emojinos.ai :as ai]
   [emojinos.ui.elements :refer [tile-el hand-el board-el]]
   [emojinos.ui.frame :refer [ui-component]])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(defcard-rg tile-test
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
  {:board #{["🌧️" 0 0 true]
            ["🌷" 0 -1 false]
            ["🌱" -1 0 true]
            ["🔥" -2 0 false]}
   :p1 ["🍚" "💞" "🍇" "🌱" "🌱"]
   :p2 ["🌻" "🍇" "🌱" "🌱" "🌱"]
   :bag ["🌧️" "🌻" "🍄"]
   :rules [{:type :adj
            :base "🌱"
            :adj "🌧️"
            :base-to "🌻"
            :consume-adj true}]})

(defcard board-details
  (let [b (:board s1)]
    {'board b
     'targets (game/targets b)}))

(defcard-rg board-test
  [:div
   (hand-el {:hand (:p2 s1)
             :playable? false
             :white? false})
   (hand-el {:hand (:p1 s1)
             :playable? true
             :white? true})
   (board-el {:board (:board s1)})])

(defcard rules-test
  (let [board (:board s1)
        rules (:rules s1)]
    {:rules rules
     :tiles board

     "the tile at [-2 0]"
     (game/get-tile-at -2 0 board)
     "neighbors of [-1 0]"
     (game/get-neighbors -1 0 board)
     "a neighbor of 🌱 matches rule #1?"
     (game/a-neighbor-matches? ["🌱" -1 0 true]
                               board
                               (first rules))
     "changes to 🌱"
     (game/get-changes ["🌱" -1 0 true]
                       board
                       rules)
     "transformations"
     (game/get-transformations board rules)}))

(defcard-rg pairs
  (let [tile-vec->tiles (fn [tile-vec]
                          (map #(into [:div {:style {:margin 2}}]
                                      [(tile-el {:emoji %
                                                 :white? true
                                                 :blank? (not %)})])

                               tile-vec))
        pair-el (fn [[left right]]
                  [:div {:style {:display "flex"}}
                   (tile-vec->tiles left)
                   [:div {:style {:font-size 60
                                  :margin "0 15px"}}
                    "→"]
                   (tile-vec->tiles right)])
        pairs [[["🌱" "🌧️"] ["🌻" nil]]]]
    (into [:div]
          (map pair-el pairs))))

(defcard-rg frame-test
  [ui-component])
